package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Product;
import lombok.Data;

@Data
public class ResponseProductDTO {
    private Product product;
    private double price_discount;
}
