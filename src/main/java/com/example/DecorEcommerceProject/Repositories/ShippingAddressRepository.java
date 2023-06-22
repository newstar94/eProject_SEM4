package com.example.DecorEcommerceProject.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.DecorEcommerceProject.Entities.ShippingAddress;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress,Long> {
}
