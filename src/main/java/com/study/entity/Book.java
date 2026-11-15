package com.study.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Book {
    private int id;
    private String title;
    private String desc;
    private BigDecimal price;
    private boolean deleted;
}
