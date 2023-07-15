package com.example.DecorEcommerceProject.Entities;

import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.Collection;
import java.util.List;

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
    @JsonIgnore
    private String password;
    @Column( nullable = false)
    private String email;
//    @Column(unique = true, nullable = false)
    @Column
    private String phone;
    @Column
    private String address;
    @Column
    private Level level;
    @Column
    private int point;
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles;
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "user")
    private List<Order> orders;
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,mappedBy = "user")
    private List<VoucherUser> voucherUsers;
}
