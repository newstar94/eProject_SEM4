package com.example.DecorEcommerceProject.Config;

import com.example.DecorEcommerceProject.Security.JWTAuthenticationFilter;
import com.example.DecorEcommerceProject.Security.JwtAuthEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity

public class SecurityConfig {
    private final JwtAuthEntryPoint authEntryPoint;

    //    private CustomeUserDetailsService userDetailsService;
//
//    @Autowired
//    public SecurityConfig(CustomeUserDetailsService userDetailsService) {
//        this.userDetailsService = userDetailsService;
//    }
    @Autowired
    public SecurityConfig(JwtAuthEntryPoint authEntryPoint) {
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //All Roles
    private static final String[] ALLOW_ALL_URLS = {
            //Category
            "/api/categories",
            "/api/category/{id}",

            //Product
            "/api/products/admin/products/page",
            "/api/products/index",
            "/api/products/search",
            "/api/products",                            //getAllProducts
            "/api/products/product/{id}",               //getProductByID
            "/api/products/category/{cateId}",          //getProductByCateID
            "/api/products/cateID-search",              //getProductsByCateIDAndKeyword
            "/api/products/top-sold",                   //getTopSold
            "/api/products/all-top-sold ",              //getAllTopSold
            "/api/products/total/{Id}",                 //totalByCategoryId

            //User
            "/api/users/login",
            "/api/users/add",
            "/api/users/changePassword",
            "/api/users/save/{id}",                 //updateUser

            //Discount
            "/api/discounts",
            "/api/discounts/get_by_product/{id}",
            "/api/discount/{id}",
            "/api/discount/product/{id}",



            //Vouchers
            "/api/vouchers",



            //Order
            "/api/order/user/{id}",                             //getAllOrderByUseId
            "/api/order/{id}",
            "/api/order/delivering/{id}",


    };

    //    private static final String[] ALLOW_POST_ALL_URLS = {
//            "/api/category/add",
//            "/api/products/add"
//    };
    private static final String[] ALLOW_USER_URLS = {

            //DeliveryAddress
            "/api/delivery_address/add",                        //createDeliveryAddress
            "/api/delivery_address/edit/{id}",                  //editDeliveryAddress
            "/api/delivery_address/user/{id}",                  //getAllDeliveryAddressByUserId


            //Voucher
            "/api/voucher/apply",


            //Order
            "/api/order/place_order",
            "/api/order/checkout",
            "/api/order/confirm/{id}",
            "/api/order/place_order/{id}",
            "/api/order/result",
            "/api/order/cancel/{id}",
            "/api/order/finish/{id}",
    };
    private static final String[] ALLOW_ADMIN_USER_URLS = {
            "/api/users/{id}",
    };
    private static final String[] ALLOW_ADMIN_URLS = {
            //Role
            "/api/users/role/save",
            "/api/users/role/addToUser",

            //Category
            "/api/categories/add",
            "/api/category/delete/{id}",
            "/api/category/save/{id}",

            //Product
            "/api/products/add",                        //createProduct
            "/api/products/save/{id}",
            "/api/products/delete/{id}",
            "/api/products/delete-image/{id}",

            //User
            "/api/users",
            "/api/users/get-by-level",
            "/api/users/{id}",
            "/api/users/email",
            "/api/users/phone",
            "/api/users/delete/{id}",

            //Discount
            "/api/discount/create",
            "/api/discount/update/{id}",


            //Voucher
            "/api/voucher/create",


            //Orders
            "/api/order/all",                   //getAllOrders
            "/api/order/print/{id}",
            "/api/order/delivered/{id}",
            "/api/order/accept_return/{id}",


    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(authEntryPoint)
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.authorizeRequests().antMatchers(ALLOW_ALL_URLS).permitAll();
        http.authorizeRequests().antMatchers(ALLOW_USER_URLS).hasRole("USER");
        http.authorizeRequests().antMatchers(ALLOW_ADMIN_URLS).hasRole("ADMIN");
        http.authorizeRequests().antMatchers(ALLOW_ADMIN_USER_URLS).hasAnyAuthority("USER", "ADMIN"); // Cho phép cả USER và ADMIN truy cập

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }
}
