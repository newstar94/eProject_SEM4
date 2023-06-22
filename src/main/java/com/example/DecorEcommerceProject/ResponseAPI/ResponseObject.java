package com.example.DecorEcommerceProject.ResponseAPI;

import lombok.Data;

@Data
public class ResponseObject {
    private String status;
    private String message;
    private Object data;
}
