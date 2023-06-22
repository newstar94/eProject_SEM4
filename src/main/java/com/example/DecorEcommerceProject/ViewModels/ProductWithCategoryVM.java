package com.example.DecorEcommerceProject.ViewModels;

import lombok.Data;

@Data
public class ProductWithCategoryVM {
    private String name;
    private String description;
    private int inventory;
    private double price;
    private String imageUrl;
}
