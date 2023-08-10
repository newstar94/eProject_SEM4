package com.example.DecorEcommerceProject.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "delivery_addresses")
@Data
public class DeliveryAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
//    @JsonIgnore
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "deliveryAddress")
    private List<Order> orders;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;// địa chỉ cụ thể

    @Column(nullable = false)
    private int province_id;

    @Column(nullable = false)
    private String province;// tỉnh thành phố

    @Column(nullable = false)
    private int district_id;

    @Column(nullable = false)
    private String district; //quận huyện

    @Column(nullable = false)
    private String wardCode;

    @Column(nullable = false)
    private String ward; // phường xã

    private boolean active;
}
