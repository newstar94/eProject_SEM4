package com.example.DecorEcommerceProject.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.DecorEcommerceProject.Entities.VoucherUser;

@Repository
public interface VoucherUserRepository extends JpaRepository<VoucherUser,Long> {
}
