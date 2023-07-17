package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Enum.DeliveryType;
import lombok.Data;
import java.util.List;
import com.example.DecorEcommerceProject.Entities.ShippingAddress;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Entities.Voucher;
import com.example.DecorEcommerceProject.Entities.Enum.PaymentType;

@Data
public class OrderDTO {
    private PaymentType paymentType;
    private DeliveryType deliveryType;
    private String voucherCode;
    private ShippingAddress shippingAddress;
    private User user;
    private List<OrderItemDTO> orderItemDTOS;
}
