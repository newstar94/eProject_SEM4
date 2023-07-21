package com.example.DecorEcommerceProject.Service;

import java.io.IOException;

import com.example.DecorEcommerceProject.Entities.Order;

import javax.servlet.http.HttpServletRequest;

public interface IPaymentService {
    Object createPayment(Long id, HttpServletRequest request) throws Exception;

    Object getResult(String vnp_TmnCode, String vnp_Amount, String vnp_BankCode, String vnp_BankTranNo, String vnp_CardType, String vnp_PayDate, String vnp_OrderInfo, String vnp_TransactionNo, String vnp_ResponseCode, String vnp_TransactionStatus, String vnp_TxnRef, String vnp_SecureHash) throws IOException;

    boolean refund(Order order, HttpServletRequest request) throws IOException;
}
