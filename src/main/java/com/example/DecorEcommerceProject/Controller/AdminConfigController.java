package com.example.DecorEcommerceProject.Controller;

import com.example.DecorEcommerceProject.Entities.AdminConfig;
import com.example.DecorEcommerceProject.Service.IAdminConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin_config")
public class AdminConfigController {
    private final IAdminConfigService adminConfigService;

    public AdminConfigController(IAdminConfigService adminConfigService) {
        this.adminConfigService = adminConfigService;
    }

    @PutMapping("/edit")
    private ResponseEntity<?> editAdminConfig(@RequestBody AdminConfig adminConfig){
        try {
            return ResponseEntity.ok(adminConfigService.editAminConfig(adminConfig));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not edit!");
        }
    }
}
