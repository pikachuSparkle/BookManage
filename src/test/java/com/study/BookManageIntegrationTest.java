package com.study;

import com.study.config.*;
import com.study.entity.AddBookForm;
import com.study.service.BookService;
import com.study.service.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig
@ContextHierarchy({
        @ContextConfiguration(name = "root", classes = MainConfiguration.class),
        @ContextConfiguration(name = "web", classes = {MvcConfiguration.class, SecurityConfiguration.class})
})
@WebAppConfiguration
@TestPropertySource(properties = {
        "DB_URL=jdbc:h2:mem:bookmanage;MODE=MySQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER;LOCK_TIMEOUT=10000",
        "DB_USER=sa", "DB_PASSWORD="
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookManageIntegrationTest {
    @Autowired DataSource dataSource;
    @Autowired BookService books;
    @Autowired WebApplicationContext context;
    private JdbcTemplate jdbc;
    private MockMvc mvc;

    @BeforeAll
    void createSchema() {
        new ResourceDatabasePopulator(new ClassPathResource("db/schema.sql")).execute(dataSource);
        jdbc = new JdbcTemplate(dataSource);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @BeforeEach
    void seed() {
        jdbc.update("DELETE FROM persistent_logins");
        jdbc.update("DELETE FROM borrow");
        jdbc.update("DELETE FROM book");
        jdbc.update("DELETE FROM student");
        jdbc.update("DELETE FROM user");
        jdbc.update("INSERT INTO student(id,name,sex,grade) VALUES(1,'小明','男',2026),(2,'小红','女',2026)");
        jdbc.update("INSERT INTO book(id,title,`desc`,price) VALUES(10,'Java','入门',59.90),(11,'Spring','实战',89.50)");
        jdbc.update("INSERT INTO user(username,password,role) VALUES(?,?,?)", "admin",
                new BCryptPasswordEncoder().encode("test-password"), "ADMIN");
    }

    @Test
    void borrowIdsAreUnambiguousAndReturnsPreserveHistory() {
        jdbc.update("INSERT INTO borrow(id,sid,bid,time) VALUES(100,2,10,CURRENT_TIMESTAMP)");
        var borrow = books.getBorrowList().get(0);
        assertEquals(100, borrow.getId());
        assertEquals(2, borrow.getSid());
        assertEquals(10, borrow.getBid());
        assertEquals("小红", borrow.getStudentName());
        assertEquals("Java", borrow.getBookName());
        books.returnBook(100);
        assertTrue(books.getBorrowList().isEmpty());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM borrow WHERE returned_at IS NOT NULL", Integer.class));
        assertThrows(BusinessException.class, () -> books.returnBook(100));
    }

    @Test
    void invalidAndDuplicateBorrowsAndDeletingBorrowedBooksAreRejected() {
        assertThrows(BusinessException.class, () -> books.addBorrow(999, 10));
        assertThrows(BusinessException.class, () -> books.addBorrow(1, 999));
        books.addBorrow(1, 10);
        assertThrows(BusinessException.class, () -> books.addBorrow(2, 10));
        assertThrows(BusinessException.class, () -> books.deleteBook(10));
        assertTrue(books.getBookList().get(0).isBorrowed());
        assertEquals(1, books.getActiveBookList().size());
        assertEquals(1, books.countAvailableBooks());
    }

    @Test
    void concurrentBorrowHasExactlyOneWinner() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> task = () -> {
            start.await();
            try { books.addBorrow(1, 10); return true; }
            catch (BusinessException exception) { return false; }
        };
        try {
            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(1, books.getBorrowList().size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void deletionIsSoftAndMoneyKeepsCents() {
        AddBookForm form = new AddBookForm();
        form.setTitle("精度测试"); form.setDesc("保留小数"); form.setPrice(new BigDecimal("12.34"));
        books.addBook(form);
        assertEquals(new BigDecimal("12.34"), books.getBookList().get(2).getPrice());
        form.setPrice(new BigDecimal("-1.00"));
        assertThrows(BusinessException.class, () -> books.addBook(form));
        books.addBorrow(1, 10);
        books.returnBook(books.getBorrowList().get(0).getId());
        books.deleteBook(10);
        assertEquals(2, books.countBooks());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM borrow", Integer.class));
        assertThrows(BusinessException.class, () -> books.addBorrow(1, 10));
    }

    @Test
    void csrfAndRolesAreEnforcedAndGetCannotMutate() throws Exception {
        mvc.perform(get("/books")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/delete-book").param("bid", "10").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/delete-book").param("bid", "10").with(csrf()).with(user("reader").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/delete-book").param("bid", "10").with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(get("/return-book").param("id", "100").with(user("admin").roles("ADMIN")))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/doLogout").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/doLogout").with(csrf()).with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection());
        assertEquals(2, books.countBooks());
    }

    @Test
    void realPasswordLoginAndRememberMeWork() throws Exception {
        mvc.perform(post("/doLogin").with(csrf()).param("username", "admin").param("password", "wrong"))
                .andExpect(redirectedUrl("/login?error"));
        mvc.perform(post("/doLogin").with(csrf()).param("username", "admin").param("password", "test-password")
                        .param("remember-me", "on"))
                .andExpect(redirectedUrl("/"))
                .andExpect(cookie().exists("remember-me"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM persistent_logins", Integer.class));
    }

    @Test
    void everyPageRendersAndPostFormsIncludeCsrf() throws Exception {
        for (String path : new String[]{"/", "/borrow", "/books", "/student", "/students", "/add-book", "/add-borrow"}) {
            String html = mvc.perform(get(path).with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertTrue(html.contains("name=\"_csrf\""), path);
            assertFalse(html.contains("th:"), path);
        }
        String html = mvc.perform(get("/books").with(user("reader").roles("USER")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(html.contains("action=\"/delete-book\""));
        mvc.perform(get("/static/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void nonRootDeploymentUsesContextAwareLinks() throws Exception {
        String html = mvc.perform(get("/library/login").contextPath("/library"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("action=\"/library/doLogin\""));
        assertTrue(html.contains("<base href=\"/library/\""));
        assertTrue(html.contains("name=\"_csrf\""));
    }

    @Test
    void validationAndBusinessErrorsRenderSafeMessages() throws Exception {
        mvc.perform(post("/add-book").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("title", "").param("desc", "test").param("price", "-1"))
                .andExpect(status().isBadRequest()).andExpect(view().name("error"));
        mvc.perform(post("/add-borrow").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("student", "999").param("book", "10"))
                .andExpect(status().isConflict()).andExpect(view().name("error"));
        mvc.perform(post("/return-book").with(user("admin").roles("ADMIN")).with(csrf()).param("id", "bad"))
                .andExpect(status().isBadRequest());
    }
}
