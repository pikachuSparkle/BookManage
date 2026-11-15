package com.study.service;

import com.study.entity.*;
import java.util.List;

public interface BookService {
    List<Borrow> getBorrowList();
    List<BookStatus> getBookList();
    List<Book> getActiveBookList();
    int countBooks();
    int countAvailableBooks();
    void addBorrow(int sid, int bid);
    void returnBook(int id);
    void addBook(AddBookForm form);
    void deleteBook(int bid);
}
