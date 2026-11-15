package com.study.mapper;

import com.study.entity.Book;
import com.study.entity.BookStatus;
import com.study.entity.Borrow;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

public interface BookMapper {
    @Select("""
            SELECT br.id, br.sid, br.bid, br.time, br.returned_at AS returnedAt,
                   s.name AS studentName, b.title AS bookName
            FROM borrow br
            JOIN student s ON s.id = br.sid
            JOIN book b ON b.id = br.bid
            WHERE br.returned_at IS NULL
            ORDER BY br.id
            """)
    List<Borrow> getBorrowList();

    @Select("""
            SELECT b.id, b.title, b.`desc`, b.price,
                   EXISTS(SELECT 1 FROM borrow br WHERE br.bid = b.id AND br.returned_at IS NULL) AS borrowed
            FROM book b WHERE b.deleted = FALSE ORDER BY b.id
            """)
    List<BookStatus> getBookList();

    @Select("""
            SELECT b.id, b.title, b.`desc`, b.price FROM book b
            WHERE b.deleted = FALSE
              AND NOT EXISTS(SELECT 1 FROM borrow br WHERE br.bid = b.id AND br.returned_at IS NULL)
            ORDER BY b.id
            """)
    List<Book> getActiveBookList();

    @Select("SELECT id, title, `desc`, price, deleted FROM book WHERE id = #{id} FOR UPDATE")
    Book lockBook(int id);

    @Select("SELECT COUNT(*) FROM borrow WHERE bid = #{bid} AND returned_at IS NULL")
    int countActiveBorrows(int bid);

    @Select("SELECT bid FROM borrow WHERE id = #{id}")
    Integer findBorrowBookId(int id);

    @Insert("INSERT INTO borrow (sid, bid, time) VALUES (#{sid}, #{bid}, CURRENT_TIMESTAMP)")
    void addBorrow(@Param("sid") int sid, @Param("bid") int bid);

    @Update("UPDATE borrow SET returned_at = CURRENT_TIMESTAMP WHERE id = #{id} AND returned_at IS NULL")
    int returnBook(int id);

    @Update("UPDATE book SET deleted = TRUE WHERE id = #{id} AND deleted = FALSE")
    int deleteBook(int id);

    @Insert("INSERT INTO book (title, `desc`, price) VALUES (#{title}, #{desc}, #{price})")
    void addBook(@Param("title") String title, @Param("desc") String desc, @Param("price") BigDecimal price);

    @Select("SELECT COUNT(*) FROM book WHERE deleted = FALSE")
    int countBooks();

    @Select("""
            SELECT COUNT(*) FROM book b WHERE b.deleted = FALSE
            AND NOT EXISTS(SELECT 1 FROM borrow br WHERE br.bid = b.id AND br.returned_at IS NULL)
            """)
    int countAvailableBooks();
}
