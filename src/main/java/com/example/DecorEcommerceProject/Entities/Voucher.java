package com.example.DecorEcommerceProject.Entities;

import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany (fetch = FetchType.LAZY, mappedBy = "voucher")
    private List<Order> orders;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end;

    @Column(name = "usage_limit", nullable = false)
    private int limit;

    @Column(name = "percentage", nullable = false)
    private int percentage;

    @Column(name = "amount_max", nullable = false)
    private int amountMax;

    @Column(nullable = false)
    private Level level;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "voucher")
    private List<VoucherUser> voucherUsers;
}
