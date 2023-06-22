package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.DTO.RegisterRequest;
import com.example.DecorEcommerceProject.Entities.Role;
import com.example.DecorEcommerceProject.Entities.Token.PasswordResetToken;
import com.example.DecorEcommerceProject.Entities.User;
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
import java.util.*;

@Service

public class UserServiceImpl implements IUserService{
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private PasswordResetTokenRepository passwordResetTokenRepository;
    public UserServiceImpl(UserRepository userRepository,RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                           PasswordResetTokenRepository passwordResetTokenRepository){
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
                return "User " + phone + " has already have role " + roleName + " !";
            }
            user.getRoles().add(role);
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
        return userRepository.findById(id).orElseThrow(()->new EntityNotFoundException("User not found with id " + id));
    }

    @Override
    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setAddress(user.getAddress());
        // Update other fields as necessary

        return userRepository.save(existingUser);
    }

    @Override
    public String deleteUser(Long id) {
        User user = userRepository.findById(id).get();
        if(user == null){
            return "Cannot find User " +id;
        }else{
            userRepository.delete(user);
            return "User "+id+ " has been deleted !";
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserByPhone(String phone) {
        return  userRepository.findUserByPhone(phone);
    }

    @Override
    public User updateUserByLoggedIn(User user) {
        user.setName(user.getName());
        user.setUsername(user.getUsername());
        user.setEmail(user.getEmail());
        user.setPhone(user.getPhone());
        user.setAddress(user.getAddress());
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

        if(passwordResetToken == null) {
            return "invalid";
        }

        User user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();

        if((passwordResetToken.getExpirationTime().getTime()
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
        return passwordEncoder.matches(oldPassword,user.getPassword());
    }

    @Override
    public UserDetails login(String phone, String password) {
        User user = userRepository.findUserByPhone(phone);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with phone: " + phone);
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
    public User register(RegisterRequest model) {
        User user = new User();
        user.setUsername(model.getUsername());
        user.setName(model.getName());
        user.setPassword(passwordEncoder.encode(model.getPassword()));
        user.setPhone(model.getPhone());
        user.setEmail(model.getEmail());
        user.setAddress(model.getAddress());
        Role roles = roleRepository.findByName("USER");
        user.setRoles(Collections.singletonList(roles));
        return userRepository.save(user);



    }

//    @Override
//    public User findUserByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }


}
