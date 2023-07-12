package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Security.SecurityConstants;
import lombok.Data;

@Data
public class AuthResponseDto {
    private String username;
    private String accessToken;
    private String tokenType = "Bearer";
    private long  tokenExpiration = SecurityConstants.JWT_EXPIRATION;

    public AuthResponseDto(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }
}
