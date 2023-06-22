package com.example.DecorEcommerceProject.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.DecorEcommerceProject.Entities.Discount;
import com.example.DecorEcommerceProject.Entities.Product;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount,Long> {
    @Query(
            value = "SELECT p FROM Product p JOIN DiscountHistory d ON p.id = d.product.id WHERE d.discount.id = :id"
    )
    List<Product> getAllProductByDiscountId(Long id);
    @Query(
            value = "SELECT dc FROM Discount dc JOIN DiscountHistory dh ON dc.id = dh.discount.id WHERE dh.product.id = :id")
    List<Discount> getAllDiscountByProductId(Long id);
}