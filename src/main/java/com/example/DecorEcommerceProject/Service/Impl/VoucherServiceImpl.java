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

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoucherServiceImpl implements IVoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherUserRepository voucherUserRepository;
    private final UserRepository userRepository;
    private final EmailServiceImpl emailService;

    public VoucherServiceImpl(VoucherRepository voucherRepository, VoucherUserRepository voucherUserRepository, UserRepository userRepository, EmailServiceImpl emailService) {
        this.voucherRepository = voucherRepository;
        this.voucherUserRepository = voucherUserRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
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
        List<User> list = voucherDTO.getUsers();
        List<User> users = new ArrayList<>();
        for (User user : list) {
            User newUser = userRepository.findById(user.getId()).get();
            users.add(newUser);
        }
        return saveVoucherUser(users, createdVoucher);
    }

    private Voucher saveVoucherUser(List<User> users, Voucher voucher) throws MessagingException {
        if (!users.isEmpty()) {
            for (User user : users) {
                VoucherUser voucherUser = new VoucherUser();
                voucherUser.setUser(user);
                voucherUser.setVoucher(voucher);
                voucherUser.setUsed(false);
                String to = user.getEmail();
                String subject = "Voucher dành cho bạn";
                String content = "<h2>Xin chào " + user.getName() + "</h2>" +
                        "<p>Nhằm tri ân những khách hàng thân yêu của chúng tôi. Chúng tôi xin gửi tới bạn một voucher</p>" +
                        "<p>Mã voucher: " + voucher.getCode() + " </p>" +
                        "<p>Giảm giá: " + voucher.getPercentage() + "%. Tối đa: " + voucher.getAmountMax() + "</p>" +
                        "<p>Bắt đầu: " + voucher.getStart() + "</p>" +
                        "<p>Kết thúc: " + voucher.getEnd() + "</p>";
                emailService.sendEmail(to, subject, content);
                voucherUserRepository.save(voucherUser);
            }
        }
        return voucher;
    }
}

