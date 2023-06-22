package com.example.DecorEcommerceProject.Entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
