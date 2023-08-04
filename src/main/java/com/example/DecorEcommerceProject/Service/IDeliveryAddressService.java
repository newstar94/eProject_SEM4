package com.example.DecorEcommerceProject.Service;

import com.example.DecorEcommerceProject.Entities.DeliveryAddress;

import java.util.List;

public interface IDeliveryAddressService {
    DeliveryAddress createDeliveryAddress(DeliveryAddress deliveryAddress);
    DeliveryAddress editDeliveryAddress(Long Id, DeliveryAddress deliveryAddress);
    DeliveryAddress getDeliveryAddressById(Long Id);
    List<DeliveryAddress> getAllDeliveryAddressByUserId(Long Id);
}
