package com.study.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Borrow {
    private int id;
    private int sid;
    private int bid;
    private LocalDateTime time;
    private LocalDateTime returnedAt;
    private String bookName;
    private String studentName;
}
