package com.example.DecorEcommerceProject.Entities.DTO;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
@Data
public class ProductDto {
    @NotEmpty(message = "Thiếu tên sản phẩm")
    private String name;
    @NotNull
    @Min(value = 1, message= "Thiếu loại sản phẩm")
    private long category;
    private String description;
    @NotEmpty(message = "Thiếu số lượng sản phẩm")
    private int inventory;
    @NotEmpty(message = "Thiếu giá cả")
    private double price;
    private String imageUrl;
}
