package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.DeliveryAddress;
import com.example.DecorEcommerceProject.Repositories.DeliveryAddressRepository;
import com.example.DecorEcommerceProject.Service.IDeliveryAddressService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryAddressServiceImpl implements IDeliveryAddressService {
    private final DeliveryAddressRepository deliveryAddressRepository;

    public DeliveryAddressServiceImpl(DeliveryAddressRepository deliveryAddressRepository) {
        this.deliveryAddressRepository = deliveryAddressRepository;
    }

    @Override
    public DeliveryAddress createDeliveryAddress(DeliveryAddress deliveryAddress) {
        deliveryAddress.setActive(true);
        return deliveryAddressRepository.save(deliveryAddress);
    }

    @Override
    public DeliveryAddress editDeliveryAddress(Long Id, DeliveryAddress deliveryAddress) {
        DeliveryAddress existDeliveryAddress = deliveryAddressRepository.findById(Id).orElse(null);
        if (existDeliveryAddress != null) {
            existDeliveryAddress.setId(Id);
            existDeliveryAddress.setActive(false);
            deliveryAddressRepository.save(existDeliveryAddress);
            deliveryAddress.setUser(existDeliveryAddress.getUser());
            deliveryAddress.setActive(true);
            return deliveryAddressRepository.save(deliveryAddress);
        }
        return null;
    }

    @Override
    public List<DeliveryAddress> getAllDeliveryAddressByUserId(Long Id) {
        return deliveryAddressRepository.getAllDeliveryAddressByUserId(Id);
    }
}
