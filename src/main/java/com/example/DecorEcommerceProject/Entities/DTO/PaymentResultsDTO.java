package com.example.DecorEcommerceProject.Entities.DTO;

import com.example.DecorEcommerceProject.Entities.Order;

import lombok.Data;
@Data
public class PaymentResultsDTO {
    private String amount;
    private String bankCode;
    private String bankTranNo;
    private String orderInfo;
    private String payDate;
    private String responseCode;
    private String transactionNo;
    private String transactionStatus;
    private String TxnRef;
    private Order order;
}
