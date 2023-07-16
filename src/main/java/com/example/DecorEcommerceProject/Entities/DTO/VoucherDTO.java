package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Entities.Voucher;
import lombok.Data;

import java.util.List;
@Data
public class VoucherDTO {
    private Voucher voucher;
    private Level level;
    private List<User> users;
}
