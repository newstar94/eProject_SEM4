package com.example.DecorEcommerceProject;

import com.example.DecorEcommerceProject.Entities.AdminConfig;
import com.example.DecorEcommerceProject.Repositories.AdminConfigRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class DecorEcommerceProjectApplication {
    private final AdminConfigRepository adminConfigRepository;

    public DecorEcommerceProjectApplication(AdminConfigRepository adminConfigRepository) {
        this.adminConfigRepository = adminConfigRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(DecorEcommerceProjectApplication.class, args);
    }

    @PostConstruct
    public void init() {
        if (adminConfigRepository.count() == 0) {
            AdminConfig adminConfig = new AdminConfig();
            adminConfig.setDelivery_fee(8000);
            adminConfig.setVnp_apiUrl("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");
            adminConfig.setVnp_PayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
            adminConfig.setVnp_ReturnUrl("http://localhost:9090/api/order/result");
            adminConfig.setVnp_TmnCode("NVWUQGML");
            adminConfig.setVnp_HashSecret("VJVDYZJTDGQAZGAJUQRZAVKMEZEECJJM");
            adminConfigRepository.save(adminConfig);
        }
    }

}
