package com.study.controller;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {




    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @GetMapping("/login")
    public String login() {
        logger.info("this is for testing -------- enter login controller");

        return "login";
    }

/*

    @GetMapping("/")
    public String index() {
        return "/template";
    }
*/
}
