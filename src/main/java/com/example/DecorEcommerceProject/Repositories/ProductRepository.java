package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.DTO.SoldDTO;
import com.example.DecorEcommerceProject.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(
            value = "SELECT * FROM products p where p.category_id = :cateId",
            nativeQuery = true
    )
    List<Product> getAllProductByCategoryID(Long cateId);

    @Query(
            value = "SELECT * from products p " +
                    "where p.name like %:keyword% ",
            nativeQuery = true
    )
    List<Product> getAllProductsByKeyword(String keyword);

    @Query(
            "SELECT NEW com.example.DecorEcommerceProject.Entities.DTO.SoldDTO( oi.product, SUM(oi.quantity)) FROM OrderItem oi GROUP BY oi.product " +
                    "ORDER BY SUM(oi.quantity) DESC")
    List<SoldDTO> getTopSelling();


}
