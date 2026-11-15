package com.study.service.impl;

import com.study.entity.Account;
import com.study.entity.Student;
import com.study.mapper.UserMapper;
import com.study.service.UserService;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper mapper;
    public UserServiceImpl(UserMapper mapper) { this.mapper = mapper; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = mapper.findUserByName(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User.withUsername(account.getUsername()).password(account.getPassword())
                .roles(account.getRole().toUpperCase(Locale.ROOT)).build();
    }

    public List<Student> getStudentList() { return mapper.getStudentList(); }
    public int countStudents() { return mapper.countStudents(); }
}
