package com.study.entity;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AddBookForm {
    @NotBlank(message = "书名不能为空")
    @Size(max = 255, message = "书名最多255字")
    private String title;
    @NotBlank(message = "简介不能为空")
    @Size(max = 255, message = "简介最多255字")
    private String desc;
    @NotNull(message = "请输入价格")
    @DecimalMin(value = "0.00", message = "价格不能为负数")
    @Digits(integer = 8, fraction = 2, message = "价格最多8位整数和2位小数")
    private BigDecimal price;
}
