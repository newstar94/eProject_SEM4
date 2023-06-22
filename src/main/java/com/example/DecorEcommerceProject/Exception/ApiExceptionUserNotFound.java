package com.example.DecorEcommerceProject.Exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Data
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ApiExceptionUserNotFound extends RuntimeException{
    private String message;
    public ApiExceptionUserNotFound(String message) {
        super(message);
        this.message = message;
    }
}
