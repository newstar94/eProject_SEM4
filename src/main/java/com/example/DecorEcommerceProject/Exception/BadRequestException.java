package com.example.DecorEcommerceProject.Exception;

import com.example.DecorEcommerceProject.ResponseAPI.ResponseObject;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@Data
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException{
    private ResponseObject responseObject;
    public BadRequestException(ResponseObject responseObject) {
        super();
        this.responseObject = responseObject;
    }
    public BadRequestException(String message) {
        super(message);
    }
    public ResponseObject getResponseObject() {
        return responseObject;
    }
}
