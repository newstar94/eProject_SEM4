package com.example.DecorEcommerceProject.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.DecorEcommerceProject.Entities.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query(value = "SELECT * FROM orders WHERE user_id = :UserId",nativeQuery = true)
    List<Order> findAllByUserId(Long UserId);
    @Query(value = "SELECT * FROM orders WHERE status = 'DELIVERED'",nativeQuery = true)
    List<Order> findAllDeliveredOrder();
    @Query(value = "SELECT * FROM orders WHERE status = 'DELIVERING' AND delivery_type = 0",nativeQuery = true)
    List<Order> findAllDeliveringOrder();
}
