package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
public class ProductTopSellerDto {
//    private long id;
//    private String name;
//    private String description;
//    private int inventory;
//    private double price;
//    private String mainImage;
//    private double totalBuy;
//    private List<String> extraImages;
    private Long productId;
    private String productName;
    private double totalSales;
}
