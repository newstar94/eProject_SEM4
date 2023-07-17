package com.example.DecorEcommerceProject.Entities;

import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "voucher")
    @JsonIgnore
    private List<Order> orders;

    @Column(name = "code",nullable = false, unique=true)
    private String code;

    @Column(name = "start_date")
    private LocalDateTime start;

    @Column(name = "end_date")
    private LocalDateTime end;

    @Column(name = "usage_limit")
    private int limit;

    @Column(name = "percentage", nullable = false)
    private int percentage;

    @Column(name = "amount_max", nullable = false)
    private int amountMax;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "voucher")
    private List<VoucherUser> voucherUsers;
}
