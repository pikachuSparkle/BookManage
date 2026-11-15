package com.study.controller;

import com.study.entity.AddBookForm;
import com.study.service.BookService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookController {
    private final BookService books;
    public BookController(BookService books) { this.books = books; }

    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("book_list", books.getBookList());
        return "books";
    }

    @GetMapping("/add-book")
    public String addBook() { return "add-book"; }

    @PostMapping("/add-book")
    public String addBook(@Valid @ModelAttribute AddBookForm form) {
        books.addBook(form);
        return "redirect:/books";
    }

    @PostMapping("/delete-book")
    public String deleteBook(@RequestParam int bid) {
        books.deleteBook(bid);
        return "redirect:/books";
    }
}
