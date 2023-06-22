package com.example.DecorEcommerceProject.Config;

import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
@Service
@Slf4j
public class CustomeUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    @Autowired
    public CustomeUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phone)  {
        User user = userRepository.findUserByPhone(phone);
        if(user == null) {
            log.error("User not found in the database");
//            throw new UsernameNotFoundException("User not found in the database");
        } else {
            log.info("User found in the database: {}",user);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getPhone(), user.getPassword(), authorities);
    }
}
