package com.example.DecorEcommerceProject.Controller;

import com.example.DecorEcommerceProject.Entities.DeliveryAddress;
import com.example.DecorEcommerceProject.Service.IDeliveryAddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery_address")
public class DeliveryAddressController {
    private final IDeliveryAddressService deliveryAddressService;

    public DeliveryAddressController(IDeliveryAddressService deliveryAddressService) {
        this.deliveryAddressService = deliveryAddressService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> createDeliveryAddress(@RequestBody DeliveryAddress deliveryAddress) {
        try {
            return ResponseEntity.ok(deliveryAddressService.createDeliveryAddress(deliveryAddress));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not add delivery address");
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editDeliveryAddress(@PathVariable Long id, @RequestBody DeliveryAddress deliveryAddress) {
        try {
            return ResponseEntity.ok(deliveryAddressService.editDeliveryAddress(id, deliveryAddress));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not edit delivery address");
        }
    }

    @GetMapping("user/{id}")
    public ResponseEntity<?> getAllDeliveryAddressByUserId(@PathVariable Long id) {
        if (deliveryAddressService.getAllDeliveryAddressByUserId(id).size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List delivery address is empty!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(deliveryAddressService.getAllDeliveryAddressByUserId(id));
    }
}
