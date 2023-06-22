package com.example.DecorEcommerceProject.Service;

import java.util.List;
import java.util.Optional;

import com.example.DecorEcommerceProject.Entities.Discount;
import com.example.DecorEcommerceProject.Entities.Product;
import com.example.DecorEcommerceProject.Entities.DTO.DiscountDTO;

public interface IDiscountService {
    Discount createDiscount(DiscountDTO discountDTO);
    Discount updateDiscount(Long id, DiscountDTO discountDTO);
    List<Discount> getAllDiscount();
    Optional<DiscountDTO> getDiscountById(Long id);
    List<Discount> getAllDiscountByProductId(Long id);
    List<Product> getAllProductByDiscountId(Long id);
}
