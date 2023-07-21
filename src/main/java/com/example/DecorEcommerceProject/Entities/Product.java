package com.example.DecorEcommerceProject.Entities;


import com.example.DecorEcommerceProject.Entities.Enum.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private int inventory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status;

    @Column(name = "unit_price")
    private double price;
    @Column(name = "main_image", nullable = false)
    private String mainImage;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_updated")
    private LocalDateTime updatedAt;

    @Column
    private int weight; //đơn vị gram

    @Column
    private boolean deliveryAvailable;

    @Column
    private int total_sold;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "product")
    private List<DiscountHistory> discountHistories;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductImage> images;
}

