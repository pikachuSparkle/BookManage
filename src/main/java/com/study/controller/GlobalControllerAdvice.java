package com.study.controller;

import com.study.service.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@ControllerAdvice
public class GlobalControllerAdvice {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @ModelAttribute
    public void currentUser(Authentication authentication, Model model) {
        model.addAttribute("nickname", authentication == null ? "" : authentication.getName());
        model.addAttribute("isAdmin", authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String businessError(BusinessException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String validationError(BindException exception, Model model) {
        model.addAttribute("message", exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage()).findFirst().orElse("输入格式不正确"));
        return "error";
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String parameterError(Model model) {
        model.addAttribute("message", "请求参数缺失或格式不正确");
        return "error";
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String databaseError(DataAccessException exception, Model model) {
        LOG.error("Database operation failed", exception);
        model.addAttribute("message", "数据库操作失败，请稍后重试");
        return "error";
    }
}
