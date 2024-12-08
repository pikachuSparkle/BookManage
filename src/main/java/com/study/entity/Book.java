package com.study.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Book {

    int id;
    String title;
    String desc;
    double price;
}
