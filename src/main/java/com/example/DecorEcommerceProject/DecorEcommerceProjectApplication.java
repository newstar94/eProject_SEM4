package com.example.DecorEcommerceProject;

import com.example.DecorEcommerceProject.Entities.AdminConfig;
import com.example.DecorEcommerceProject.Entities.Role;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Repositories.AdminConfigRepository;
import com.example.DecorEcommerceProject.Repositories.RoleRepository;
import com.example.DecorEcommerceProject.Repositories.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
public class DecorEcommerceProjectApplication {
    private final AdminConfigRepository adminConfigRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public DecorEcommerceProjectApplication(AdminConfigRepository adminConfigRepository, RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.adminConfigRepository = adminConfigRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(DecorEcommerceProjectApplication.class, args);
    }

    @PostConstruct
    public void init() {
        if (adminConfigRepository.count() == 0) {
            AdminConfig adminConfig = new AdminConfig();
            adminConfig.setVnp_apiUrl("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");
            adminConfig.setVnp_PayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
            adminConfig.setVnp_ReturnUrl("http://localhost:5173/order/payment");
            adminConfig.setVnp_TmnCode("NVWUQGML");
            adminConfig.setVnp_HashSecret("VJVDYZJTDGQAZGAJUQRZAVKMEZEECJJM");
            adminConfig.setDelivery_fee(40000);
            adminConfig.setAmount_to_free(10000000);
            adminConfig.setMax_distance(5);
            adminConfig.setDelivery_fee_km(9000);
            adminConfig.setGhn_token("6ab4fe69-fe4e-11ed-b678-22ca76951087");
            adminConfig.setShop_id("124400");
            adminConfig.setGhn_fee_url("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee");
            adminConfig.setGhn_create_url("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create");
            adminConfig.setGhn_print_url("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/a5/gen-token");
            adminConfig.setMap_url("http://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix");
            adminConfig.setMap_token("AsUeP0wj7060aELgchJgAXeswbHWrY5EmLw0NqMuhhaTBafzXyTSyedRWGmqxF58");
            adminConfig.setAddress("158 Nguyễn Khánh Toàn, Quan Hoa, Cầu Giấy, Hà Nội");
            adminConfigRepository.save(adminConfig);
        }
        if (roleRepository.count() == 0) {
            List<Role> roleList = new ArrayList<>();
            Role role1 = new Role();
            role1.setName("ADMIN");
            roleList.add(role1);
            Role role2 = new Role();
            role2.setName("USER");
            roleList.add(role2);
            Role role3 = new Role();
            role3.setName("EMPLOYEE");
            roleList.add(role3);
            roleRepository.saveAll(roleList);
        }
        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("ADMIN");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setPhone("0912345678");
            user.setName("ADMIN");
            user.setEmail("admin@admin.com");
            Role roles = roleRepository.findByName("ADMIN");
            user.setRoles(Collections.singletonList(roles));
            userRepository.save(user);
        }
    }

}
