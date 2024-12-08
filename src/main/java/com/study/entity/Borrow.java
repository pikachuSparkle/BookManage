package com.study.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Data
@Getter
@Setter
public class Borrow {

    int id;
    int sid;
    int bid;
    Date time;
    String bookName;
    String student2Name;


}
