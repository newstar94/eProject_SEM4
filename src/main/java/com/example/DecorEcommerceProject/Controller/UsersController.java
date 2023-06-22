package com.example.DecorEcommerceProject.Controller;

import com.example.DecorEcommerceProject.Entities.DTO.*;
import com.example.DecorEcommerceProject.Entities.Role;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Security.JwtGenerator;
import com.example.DecorEcommerceProject.Service.IUserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private IUserService userService;
    private AuthenticationManager authenticationManager;
    private JwtGenerator jwtGenerator;
    public UsersController(IUserService userService, AuthenticationManager authenticationManager,JwtGenerator jwtGenerator){
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
    }
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @PostMapping("/add")
    public ResponseEntity<?> createUser(@Validated @RequestBody RegisterRequest user) {
        User existedUser = userService.findUserByPhone(user.getPhone());
        if(existedUser != null){
            return  ResponseEntity.badRequest().body("Cannot create this account. Phone has existed!");
        }else {
            User createdUser = userService.register(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
//            return ResponseEntity.ok().body("Created Successful");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequest model){
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(model.getPhone(),model.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generatorToken(authentication);
        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam("email") String email) {
        User user = userService.getByEmail(email);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/phone")
    public ResponseEntity<User> getUserByPhone(@RequestParam("phone")String phone) {
        User user = userService.findUserByPhone(phone);
        return ResponseEntity.ok(user);
    }
    @PutMapping("/save/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userUpdate) {
        User user = userService.updateUser(id,userUpdate);
        return ResponseEntity.ok(user);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/role/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));
    }
    @PostMapping("/role/addtouser")
    public String addRoleToUser(@RequestBody RoleToUser toUser) throws Exception {
        return userService.addRoleToUser(toUser.getPhone(), toUser.getRoleName());
    }
    @PostMapping("/changePassword")
    public String changePassword(@RequestBody ChangePassword changePassword) {
        User user = userService.findUserByPhone(changePassword.getPhone());
        if(!userService.checkIfValidOldPassword(user, changePassword.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save new password
        userService.changePassword(user, changePassword.getNewPassword());
        return "Password changed successfully";
    }

}
