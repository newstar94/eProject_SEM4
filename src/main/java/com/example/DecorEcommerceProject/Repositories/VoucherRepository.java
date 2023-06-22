package com.example.DecorEcommerceProject.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.DecorEcommerceProject.Entities.Voucher;
@Repository
public interface VoucherRepository extends JpaRepository<Voucher,Long> {
}
