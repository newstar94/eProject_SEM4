package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.DTO.ResponseProductDTO;
import com.example.DecorEcommerceProject.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
            value = "SELECT * FROM  products p WHERE p.category.id = :categoryId ",
            nativeQuery = true
    )
    Page<ResponseProductDTO> findAllInCategory(long categoryId, Pageable pageable);
    @Query(
            value = "SELECT * FROM products p WHERE p.name LIKE  %:keyword%"
            + "OR p.description LIKE %:keyword%"
            + "OR p.category.name LIKE %:keyword%",
            nativeQuery = true
    )
    Page<ResponseProductDTO> findAllProducts(String keyword, Pageable pageable);

    @Query(
            value = "SELECT * from products p " +
                    "where p.name like %:keyword% ",
            nativeQuery = true
    )
    List<Product> getAllProductsByKeyword(String keyword);

    @Query(
            value = "SELECT * FROM products " +
                    "ORDER BY total_sold DESC LIMIT :top",
            nativeQuery = true)
    List<Product> getTopSelling(int top);

    @Query(
            value = "SELECT COUNT (*) FROM Product WHERE category.id = :Id"
    )
    Integer getTotalByCategoryId(Long Id);
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId "
            + "OR p.description LIKE %:keyword% "
            + "OR p.name LIKE %:keyword% "
            + "OR p.category.name LIKE %:keyword%")
    Page<ResponseProductDTO> searchInCategory(long categoryId, String keyword, Pageable pageable);

}
