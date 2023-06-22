package com.example.DecorEcommerceProject.Entities.DTO;

import lombok.Data;
import java.util.List;

import com.example.DecorEcommerceProject.Entities.Discount;
import com.example.DecorEcommerceProject.Entities.Product;

@Data
public class DiscountDTO {
    private Discount discount;
    private List<Product> products;
}