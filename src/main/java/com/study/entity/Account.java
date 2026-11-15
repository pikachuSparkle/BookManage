package com.study.entity;

import lombok.Data;
import lombok.ToString;

@Data
public class Account {
    private int id;
    private String username;
    @ToString.Exclude
    private String password;
    private String role;
}
