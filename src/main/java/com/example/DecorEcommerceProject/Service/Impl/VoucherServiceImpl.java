package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.DTO.VoucherDTO;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Entities.VoucherUser;
import com.example.DecorEcommerceProject.Repositories.UserRepository;
import com.example.DecorEcommerceProject.Repositories.VoucherRepository;
import com.example.DecorEcommerceProject.Repositories.VoucherUserRepository;
import org.springframework.stereotype.Service;

import com.example.DecorEcommerceProject.Entities.Voucher;
import com.example.DecorEcommerceProject.Service.IVoucherService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherServiceImpl implements IVoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherUserRepository voucherUserRepository;
    private final UserRepository userRepository;

    public VoucherServiceImpl(VoucherRepository voucherRepository, VoucherUserRepository voucherUserRepository, UserRepository userRepository) {
        this.voucherRepository = voucherRepository;
        this.voucherUserRepository = voucherUserRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Voucher> getAllVoucher() {
        return voucherRepository.findAll();
    }

    @Override
    public boolean checkVoucherAvailable(String voucherCode, String username) {
        Voucher voucher = voucherRepository.findByCode(voucherCode);
        if (voucher.getStart().isBefore(LocalDateTime.now()) && voucher.getEnd().isAfter(LocalDateTime.now())) {
            User user = userRepository.findByUsername(username);
            if (voucher != null && user != null) {
                if (voucher.getLimit() == 0) {
                    return false;
                }
                VoucherUser voucherUser = voucherUserRepository.findVoucherUserByUserIdAndVoucherId(user.getId(), voucher.getId());
                if (voucherUser != null) {
                    return !voucherUser.isUsed();
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public Voucher createVoucher(VoucherDTO voucherDTO) throws Exception {
        if (voucherDTO.getUsers().isEmpty()) {
            throw new Exception("Can not create voucher");
        }
        Voucher createdVoucher = voucherRepository.save(voucherDTO.getVoucher());
        return saveVoucherUser(voucherDTO, createdVoucher);
    }

    private Voucher saveVoucherUser(VoucherDTO voucherDTO, Voucher voucher) {
        if (!voucherDTO.getUsers().isEmpty()) {
            List<User> users = voucherDTO.getUsers();
            for (User user : users) {
                VoucherUser voucherUser = new VoucherUser();
                voucherUser.setUser(user);
                voucherUser.setVoucher(voucher);
                voucherUser.setUsed(false);
                voucherUserRepository.save(voucherUser);
            }
        }
        return voucher;
    }
}

