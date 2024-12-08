package com.study.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Setter
@Getter
public class Account {


    int id;
    String username;
    String password;
    String role;

}
