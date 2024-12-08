package com.study.service;

import com.study.entity.Borrow;
import com.study.entity.Student2;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    List<Student2> getStudent2List();

}
