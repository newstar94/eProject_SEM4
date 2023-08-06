package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.DeliveryAddress;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String phone;
    private DeliveryAddress deliveryAddress;
}
