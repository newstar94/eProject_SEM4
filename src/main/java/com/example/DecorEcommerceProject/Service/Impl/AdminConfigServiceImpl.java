package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.AdminConfig;
import com.example.DecorEcommerceProject.Repositories.AdminConfigRepository;
import com.example.DecorEcommerceProject.Service.IAdminConfigService;
import org.springframework.stereotype.Service;

@Service
public class AdminConfigServiceImpl implements IAdminConfigService {
    private final AdminConfigRepository adminConfigRepository;

    public AdminConfigServiceImpl(AdminConfigRepository adminConfigRepository) {
        this.adminConfigRepository = adminConfigRepository;
    }
    @Override
    public AdminConfig editAminConfig(AdminConfig adminConfig) {
        AdminConfig existAdminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
        Long Id = existAdminConfig.getId();
        existAdminConfig = adminConfig;
        existAdminConfig.setId(Id);
        return adminConfigRepository.save(existAdminConfig);
    }
}
