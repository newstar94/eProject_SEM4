package com.example.DecorEcommerceProject.Repositories;

import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.example.DecorEcommerceProject.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findUserByPhone(String phone);
    List<User> findByLevel(Level level);
}
