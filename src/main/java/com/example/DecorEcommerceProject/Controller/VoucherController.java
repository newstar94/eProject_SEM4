package com.example.DecorEcommerceProject.Controller;

import com.example.DecorEcommerceProject.Entities.DTO.VoucherDTO;
import com.example.DecorEcommerceProject.Entities.Voucher;
import com.example.DecorEcommerceProject.Service.IVoucherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class VoucherController {
    private final IVoucherService voucherService;

    public VoucherController(IVoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping("/vouchers")
    public ResponseEntity<?> getAllVoucher() {
        if (voucherService.getAllVoucher().size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List voucher is empty!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(voucherService.getAllVoucher());
    }
    @GetMapping("/voucher/apply")
    public ResponseEntity<?> checkVoucherIsUsed(
            @RequestParam("voucherCode") String voucherCode,
            @RequestParam("username") String username) {
        if (voucherService.checkVoucherAvailable(voucherCode, username)){
            return ResponseEntity.status(HttpStatus.OK).body("Áp mã voucher thành công");
        }
            return ResponseEntity.status(HttpStatus.OK).body("Voucher không hợp lệ hoặc đã hết lượt sử dụng!");
    }

    @PostMapping("voucher/create")
    public ResponseEntity<?> createVoucher(@Validated @RequestBody VoucherDTO voucherDTO) {
        try {
            Voucher voucher = voucherService.createVoucher(voucherDTO);
            return ResponseEntity.ok(voucher);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not create voucher!");
        }
    }
}
