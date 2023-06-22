package com.example.DecorEcommerceProject.Service.Impl;

import org.springframework.stereotype.Service;

import com.example.DecorEcommerceProject.Entities.Voucher;
import com.example.DecorEcommerceProject.Service.IVoucherService;

@Service
public class VoucherServiceImpl implements IVoucherService {
    @Override
    public Voucher createVoucher(Voucher voucher){
        return voucher;
    }
}

