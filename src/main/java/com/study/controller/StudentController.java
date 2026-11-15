package com.study.controller;

import com.study.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentController {
    private final UserService users;
    public StudentController(UserService users) { this.users = users; }

    @GetMapping({"/student", "/students"})
    public String students(Model model) {
        model.addAttribute("student_list", users.getStudentList());
        return "student";
    }
}
