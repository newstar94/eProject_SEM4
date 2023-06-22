package com.example.DecorEcommerceProject.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "discounts")
@Data
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    @Column(name = "usage_limit", nullable = false)
    @Min(value = 0)
    private int limit;

    @Column(name = "discount_percentage", nullable = false)
    private int discountPercentage;

    @Column(name = "discount_amount_max", nullable = false)
    private int discountAmountMax;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "discount")
    private List<DiscountHistory> discountHistories;
}
