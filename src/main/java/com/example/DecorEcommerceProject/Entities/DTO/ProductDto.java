package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Enum.ProductStatus;
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

    @NotEmpty(message = "Thiếu khối lượng")
    private int weight;

    @NotEmpty(message = "Có cho phép vận chuyển GHN không?")
    private boolean deliveryAvailable;

    @NotEmpty(message = "Thiếu trạng thái sản phẩm")
    private ProductStatus productStatus;

}
