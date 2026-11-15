package com.study.entity;

import lombok.Data;
import java.math.BigDecimal;

/** A single row containing both the book and its current availability. */
@Data
public class BookStatus {
    private int id;
    private String title;
    private String desc;
    private BigDecimal price;
    private boolean borrowed;
}
