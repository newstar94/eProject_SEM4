package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Enum.DeliveryType;
import lombok.Data;
import java.util.List;
import com.example.DecorEcommerceProject.Entities.DeliveryAddress;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Entities.Enum.PaymentType;

@Data
public class OrderDTO {
    private PaymentType paymentType;
    private DeliveryType deliveryType;
    private String voucherCode;
    private DeliveryAddress deliveryAddress;
    private User user;
    private List<OrderItemDTO> orderItemDTOS;
}
