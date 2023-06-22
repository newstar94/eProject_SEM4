package com.example.DecorEcommerceProject.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.DecorEcommerceProject.Entities.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
}