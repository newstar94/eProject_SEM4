package com.example.DecorEcommerceProject.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.DecorEcommerceProject.Entities.DiscountHistory;

@Repository
public interface DiscountHistoryRepository extends JpaRepository<DiscountHistory,Long> {
    void deleteByProductIdAndDiscountId(Long productId, Long discountId);
}