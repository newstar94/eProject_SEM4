package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.DTO.RegisterRequest;
import com.example.DecorEcommerceProject.Entities.DeliveryAddress;
import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.example.DecorEcommerceProject.Entities.Role;
import com.example.DecorEcommerceProject.Entities.Token.PasswordResetToken;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Exception.BadRequestException;
import com.example.DecorEcommerceProject.Repositories.DeliveryAddressRepository;
import com.example.DecorEcommerceProject.Repositories.PasswordResetTokenRepository;
import com.example.DecorEcommerceProject.Repositories.RoleRepository;
import com.example.DecorEcommerceProject.Repositories.UserRepository;
import com.example.DecorEcommerceProject.Service.IUserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.*;

@Service

public class UserServiceImpl implements IUserService {
    private DeliveryAddressRepository deliveryAddressRepository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public UserServiceImpl(DeliveryAddressRepository deliveryAddressRepository, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                           PasswordResetTokenRepository passwordResetTokenRepository) {
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Override
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public String addRoleToUser(String phone, String roleName) {
        User user = userRepository.findUserByPhone(phone);
        Role role = roleRepository.findByName(roleName);
        if (user == null) {
            return "Cannot find User with phone: " + phone + " !";
        }
        if (role == null) {
            return "Role " + roleName + " is not existed !";
        }
        if (user != null && role != null) {
            if (user.getRoles().contains(role)) {
                return "Phone " + phone + " has already have role " + roleName + " !";
            }
            user.getRoles().add(role);
            userRepository.save(user);
            return "Adding role " + roleName + " to " + phone + " successfully!";
        }
        return "";
    }

//    @Override
//    public User registerUser(User user) {
//        user.setName(user.getName());
//        user.setUsername(user.getUsername());
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setEmail(user.getEmail());
//        user.setPhone(user.getPhone());
//        user.setAddress(user.getAddress());
//        return userRepository.save(user);
//    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    @Override
    public User getByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        // Update other fields as necessary

        return userRepository.save(existingUser);
    }

    @Override
    public String deleteUser(Long id) {
        User user = userRepository.findById(id).get();
        if (user == null) {
            return "Cannot find User " + id;
        } else {
            userRepository.delete(user);
            return "User " + id + " has been deleted !";
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllUsersByLevel(Level level) {
        return userRepository.findByLevel(level);
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserByPhone(String phone) {
        return userRepository.findUserByPhone(phone);
    }


    @Override
    public User updateUserByLoggedIn(User user) {
        user.setName(user.getName());
        user.setUsername(user.getUsername());
        user.setEmail(user.getEmail());
        user.setPhone(user.getPhone());
        userRepository.save(user);
        return user;
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken
                = new PasswordResetToken(user, token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken
                = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken == null) {
            return "invalid";
        }

        User user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();

        if ((passwordResetToken.getExpirationTime().getTime()
                - cal.getTime().getTime()) <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "token has expired";
        }
        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Override
    public UserDetails login(String phone, String password) {
        User user = userRepository.findUserByPhone(phone);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with : " + phone);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid phone number or password");
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .build();
    }


    @Override
    @Transactional
    public User register(RegisterRequest model) {
        String password = model.getPassword();
        String confirmPassword = model.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new BadRequestException("Password and Confirm Password do not match.");
        }
        User user = new User();
        user.setUsername(model.getUsername());
        user.setName(model.getName());
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(model.getPhone());
        user.setEmail(model.getEmail());
        user.setPoint(0);
        user.setLevel(Level.NEW);
        Role roles = roleRepository.findByName("USER");
        user.setRoles(Collections.singletonList(roles));
        userRepository.save(user);
        DeliveryAddress deliveryAddress = model.getDeliveryAddress();
        deliveryAddress.setName(user.getName());
        deliveryAddress.setPhone(user.getPhone());
        deliveryAddress.setUser(user);
        deliveryAddress.setActive(true);
        deliveryAddressRepository.save(deliveryAddress);
        return user;
    }
}
