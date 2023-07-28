package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Product;
import lombok.Data;

@Data
public class ResponseProductDTO {
    private Product product;
    private double price_discount;
//    private int firstPage;
//    private int currentPage;
//    private int endPage;
//    private int totalPage;
//    private int totalOfProductInPage;
//    private int totalOfElement;
}
