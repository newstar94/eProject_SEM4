package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.AdminConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminConfigRepository extends JpaRepository<AdminConfig, Long> {
    AdminConfig findFirstByOrderByIdAsc();
}
