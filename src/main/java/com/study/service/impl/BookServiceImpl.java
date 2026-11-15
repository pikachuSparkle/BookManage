package com.study.service.impl;

import com.study.entity.*;
import com.study.mapper.BookMapper;
import com.study.mapper.UserMapper;
import com.study.service.BookService;
import com.study.service.BusinessException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private final BookMapper books;
    private final UserMapper users;
    private final Validator validator;

    public BookServiceImpl(BookMapper books, UserMapper users, Validator validator) {
        this.books = books;
        this.users = users;
        this.validator = validator;
    }

    public List<Borrow> getBorrowList() { return books.getBorrowList(); }
    public List<BookStatus> getBookList() { return books.getBookList(); }
    public List<Book> getActiveBookList() { return books.getActiveBookList(); }
    public int countBooks() { return books.countBooks(); }
    public int countAvailableBooks() { return books.countAvailableBooks(); }

    @Override
    @Transactional
    public void addBorrow(int sid, int bid) {
        // Every availability-changing operation locks the same book row first.
        requireAvailableBook(bid);
        if (users.countStudent(sid) == 0) {
            throw new BusinessException("学生不存在");
        }
        if (books.countActiveBorrows(bid) > 0) {
            throw new BusinessException("该图书已被借阅，请刷新页面");
        }
        books.addBorrow(sid, bid);
    }

    @Override
    @Transactional
    public void returnBook(int id) {
        Integer bid = books.findBorrowBookId(id);
        if (bid == null) {
            throw new BusinessException("借阅记录不存在");
        }
        requireAvailableBook(bid);
        if (books.returnBook(id) != 1) {
            throw new BusinessException("该图书已经归还");
        }
    }

    @Override
    public void addBook(AddBookForm form) {
        var errors = validator.validate(form);
        if (!errors.isEmpty()) {
            throw new BusinessException(errors.iterator().next().getMessage());
        }
        books.addBook(form.getTitle().trim(), form.getDesc().trim(), form.getPrice());
    }

    @Override
    @Transactional
    public void deleteBook(int bid) {
        requireAvailableBook(bid);
        if (books.countActiveBorrows(bid) > 0) {
            throw new BusinessException("图书正在借阅中，不能删除");
        }
        // Preserve historical borrow records and their foreign keys.
        books.deleteBook(bid);
    }

    private void requireAvailableBook(int bid) {
        Book book = books.lockBook(bid);
        if (book == null || book.isDeleted()) {
            throw new BusinessException("图书不存在或已删除");
        }
    }
}
