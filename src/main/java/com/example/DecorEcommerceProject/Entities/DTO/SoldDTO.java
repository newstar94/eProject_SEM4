package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Product;
import lombok.Data;

@Data
public class SoldDTO {
    private Product product;
    private Long total_sold;

    public SoldDTO(Product product, Long total_sold) {
        this.product = product;
        this.total_sold = total_sold;
    }
}
