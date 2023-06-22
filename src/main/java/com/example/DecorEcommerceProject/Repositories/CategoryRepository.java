package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
