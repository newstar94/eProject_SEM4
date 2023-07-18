package com.example.DecorEcommerceProject.Entities;

import com.example.DecorEcommerceProject.Entities.Enum.DeliveryType;
import lombok.Data;

import javax.persistence.*;

import com.example.DecorEcommerceProject.Entities.Enum.OrderStatus;
import com.example.DecorEcommerceProject.Entities.Enum.PaymentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
//    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @ManyToOne
    @JoinColumn(name = "delivery_address_id",nullable = false)
    private DeliveryAddress deliveryAddress;

    @Column
    private String ghnCode;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column
    private PaymentType paymentType;

    @Column
    private DeliveryType deliveryType;

    @Column
    private double amount;

    @Column
    private int voucher_discount;

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee;

    @Column
    private int total;
}
