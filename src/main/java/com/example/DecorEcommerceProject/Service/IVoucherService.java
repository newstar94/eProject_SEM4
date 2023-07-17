package com.example.DecorEcommerceProject.Service;

import com.example.DecorEcommerceProject.Entities.DTO.VoucherDTO;
import com.example.DecorEcommerceProject.Entities.Voucher;

import java.util.List;

public interface IVoucherService {
    List<Voucher> getAllVoucher();
    Voucher createVoucher(VoucherDTO voucherDTO) throws Exception;
    boolean checkVoucherAvailable(String voucherCode, String username);
}
