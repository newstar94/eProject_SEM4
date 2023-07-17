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
            adminConfig.setVnp_apiUrl("https://sandbox.vnpayment.vn/merchant_webapi/api/transaction");
            adminConfig.setVnp_PayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
            adminConfig.setVnp_ReturnUrl("http://localhost:9090/api/order/result");
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
            adminConfig.setMap_url("https://maps.googleapis.com/maps/api/distancematrix/json");
            adminConfig.setMap_token("AIzaSyD4wVTY8AFwxgsLzgUh9YcWcxsCMFol4g0");
            adminConfig.setAddress("158 Nguyễn Khánh Toàn, Quan Hoa, Cầu Giấy, Hà Nội");
            adminConfigRepository.save(adminConfig);
        }
    }

}
