package com.study.service.impl;

import com.study.entity.Account;
import com.study.entity.Student2;
import com.study.mapper.UserMapper;
import com.study.service.UserService;
import jakarta.annotation.Resource;
import lombok.val;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserMapper mapper;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("enter userServiceImplement");
        Account account = mapper.findUserByName(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或者密码错误，爆出异常");
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public List<Student2> getStudent2List() {
        return mapper.getStudent2List();
    }
}
