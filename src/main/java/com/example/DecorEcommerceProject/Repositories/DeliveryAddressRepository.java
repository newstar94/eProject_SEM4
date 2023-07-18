package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.DecorEcommerceProject.Entities.DeliveryAddress;

import java.util.List;

@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress,Long> {
    @Query(
            value = "SELECT * FROM delivery_addresses WHERE user_id = :id AND active = true",
            nativeQuery = true
    )
    List<DeliveryAddress> getAllDeliveryAddressByUserId(Long id);
}
