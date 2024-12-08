package com.study.controller;

import com.study.entity.Student2;
import com.study.service.BookService;
import com.study.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BorrowController {


    @Resource
    BookService bookService;

    @Resource
    UserService userService;


    @GetMapping({"borrow", "/"})
    public String borrow(Model model) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("nickname", user.getUsername());
        model.addAttribute("borrow_list", bookService.getBorrowList());
        model.addAttribute("book_count", bookService.getBookList().size());
        model.addAttribute("student_count", userService.getStudent2List().size());
        return "borrow";
    }


    @GetMapping("/add-borrow")
    public String addBorrow(Model model) {
        model.addAttribute("book_list", bookService.getActiveBookList());
        model.addAttribute("student_list", userService.getStudent2List());
        System.out.println("book_list:" + bookService.getActiveBookList());
        System.out.println("student_list:" + userService.getStudent2List());
        return "add-borrow";
    }


    @PostMapping("/add-borrow")
    public String addBorrow(int student, int book) {
        bookService.addBorrow(student, book);
        return "redirect:/borrow";
    }

    @GetMapping("/return-book")
    public String returnBook(String id){
bookService.returnBook(id);
return "redirect:/borrow";
    }



}
