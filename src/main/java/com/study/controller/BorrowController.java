package com.study.controller;

import com.study.service.BookService;
import com.study.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BorrowController {
    private final BookService books;
    private final UserService users;
    public BorrowController(BookService books, UserService users) {
        this.books = books;
        this.users = users;
    }

    @GetMapping({"/borrow", "/"})
    public String borrow(Model model) {
        model.addAttribute("borrow_list", books.getBorrowList());
        model.addAttribute("book_count", books.countBooks());
        model.addAttribute("student_count", users.countStudents());
        model.addAttribute("available_count", books.countAvailableBooks());
        return "borrow";
    }

    @GetMapping("/add-borrow")
    public String addBorrow(Model model) {
        model.addAttribute("book_list", books.getActiveBookList());
        model.addAttribute("student_list", users.getStudentList());
        return "add-borrow";
    }

    @PostMapping("/add-borrow")
    public String addBorrow(@RequestParam int student, @RequestParam int book) {
        books.addBorrow(student, book);
        return "redirect:/borrow";
    }

    @PostMapping("/return-book")
    public String returnBook(@RequestParam int id) {
        books.returnBook(id);
        return "redirect:/borrow";
    }
}
