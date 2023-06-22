package com.example.DecorEcommerceProject.Entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "voucher_users")
public class VoucherUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column
    private boolean isUsed;
}