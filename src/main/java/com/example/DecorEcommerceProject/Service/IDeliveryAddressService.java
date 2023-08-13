package com.example.DecorEcommerceProject.Service;

import com.example.DecorEcommerceProject.Entities.DeliveryAddress;

import java.util.List;

public interface IDeliveryAddressService {
    DeliveryAddress createDeliveryAddress(DeliveryAddress deliveryAddress, Long userId);
    DeliveryAddress editDeliveryAddress(Long Id, DeliveryAddress deliveryAddress);
    List<DeliveryAddress> getAllDeliveryAddressByUserId(Long Id);
}
