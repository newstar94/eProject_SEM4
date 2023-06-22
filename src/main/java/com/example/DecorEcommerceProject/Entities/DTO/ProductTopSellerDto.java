package com.example.DecorEcommerceProject.Entities.DTO;

import lombok.Data;

@Data
public class ProductTopSellerDto {
    private long id;
    private String name;
    private String description;
    private int inventory;
    private double price;
    private String imageUrl;
    private double totalBuy;
}
