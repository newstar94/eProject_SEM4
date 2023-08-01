package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Security.SecurityConstants;
import lombok.Data;

@Data
public class AuthResponseDto {
    private Long id;
    private String username;
    private String accessToken;
    private String tokenType = "Bearer";
    private long  tokenExpiration = SecurityConstants.JWT_EXPIRATION;

    public AuthResponseDto(Long id, String username, String accessToken) {
        this.id = id;
        this.username = username;
        this.accessToken = accessToken;
    }
}
