package com.example.DecorEcommerceProject.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "username", nullable = false)
    private String username;
    @Column( nullable = false)
    private String password;
    @Column( nullable = false)
    private String email;
//    @Column(unique = true, nullable = false)
    @Column
    private String phone;
    @Column
    private String address;
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles;
}
