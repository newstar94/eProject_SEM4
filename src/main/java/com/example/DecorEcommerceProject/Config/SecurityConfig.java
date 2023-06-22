package com.example.DecorEcommerceProject.Config;

import com.example.DecorEcommerceProject.Security.JWTAuthenticationFilter;
import com.example.DecorEcommerceProject.Security.JwtAuthEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private static final String[] ALLOW_ALL_URLS ={
            //Category
            "/api/categories",
            "/api/category/{id}",

            //Product
            "/api/products",
            "/api/product/{id}"
    };

//    private static final String[] ALLOW_POST_ALL_URLS = {
//            "/api/category/add",
//            "/api/products/add"
//    };
    private static final String[] ALLOW_GET_USER_URLS = {
            "/api/users/{id}",


    };
    private static final String[] ALLOW_GET_ADMIN_URLS = {
            //User
            "/api/users",
            //"/api/user/{id}"
    };
    private static final String[] ALLOW_POST_ADMIN_URLS = {

            //Role
            "/api/users/role/save",

            //Category
            "/api/categories/add",
            "/api/category/delete/{id}",
            "/api/category/save/{id}",

            //Product
            "/api/products/add",
            "/api/products/save/{id}",
            "/api/products/delete/{id}",
            "/api/products/save/{id}",
    };


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)throws Exception{
        http.csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(authEntryPoint)
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//                .and()
//                .authorizeRequests()
//                .antMatchers("/api/users/**").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .httpBasic();

        http.authorizeRequests().antMatchers(ALLOW_ALL_URLS).permitAll();
        http.authorizeRequests().antMatchers(HttpMethod.GET,ALLOW_GET_USER_URLS).hasRole("USER");
        http.authorizeRequests().antMatchers(HttpMethod.GET,ALLOW_GET_ADMIN_URLS).hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(HttpMethod.POST,ALLOW_POST_ADMIN_URLS).hasAnyAuthority("ADMIN");
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter(){
        return new JWTAuthenticationFilter();
    }
}
