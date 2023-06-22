package com.example.DecorEcommerceProject.Service;

import com.example.DecorEcommerceProject.Entities.DTO.RegisterRequest;
import com.example.DecorEcommerceProject.Entities.Role;
import com.example.DecorEcommerceProject.Entities.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    Role saveRole(Role role);
    String addRoleToUser(String phone, String roleName);
//    User registerUser(User user);
    User getUserById(Long id);
    User getUser(String username);
    User updateUser(Long id,User user);
    String deleteUser(Long id);
    List<User> getAllUsers();
//    User findUserByUsername(String username);

    User getByEmail(String email);
    User findUserByPhone(String phone);


    User updateUserByLoggedIn( User user);

    //Password Reset
    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    //Change password
    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);
    UserDetails login (String phone, String password);
    User register(RegisterRequest model);
}
