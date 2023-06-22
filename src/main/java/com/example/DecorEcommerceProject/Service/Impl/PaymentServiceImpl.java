package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Config.PaymentConfig;
import com.example.DecorEcommerceProject.Entities.Order;
import com.example.DecorEcommerceProject.Entities.User;
import com.example.DecorEcommerceProject.Entities.DTO.PaymentResultsDTO;
import com.example.DecorEcommerceProject.Entities.Enum.OrderStatus;
import com.example.DecorEcommerceProject.Entities.Enum.PaymentType;
import com.example.DecorEcommerceProject.Repositories.OrderRepository;
import com.example.DecorEcommerceProject.ResponseAPI.ResponseObject;
import com.example.DecorEcommerceProject.Service.IPaymentService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PaymentServiceImpl implements IPaymentService {
    private final OrderRepository orderRepository;
    public PaymentServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    @Override
    public Object createPayment(Order order) throws Exception {
        ResponseObject responseObject = new ResponseObject();
        if (order.getPaymentType() == PaymentType.COD) {
            responseObject.setStatus("");
            responseObject.setMessage("");
            responseObject.setData(order);
            return responseObject;
        }
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        long amount = (long) (order.getAmount() * 100L);
        String vnp_TxnRef = String.valueOf(order.getId());
        String vnp_TmnCode = PaymentConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", PaymentConfig.vnp_ReturnUrl);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_CreateDate = dtf.format(order.getCreatedAt());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = PaymentConfig.hmacSHA512(PaymentConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = PaymentConfig.vnp_PayUrl + "?" + queryUrl;
        responseObject.setStatus("");
        responseObject.setMessage(paymentUrl);
        responseObject.setData(order);
        return responseObject;
    }
    @Override
    public Object getResult(String vnp_TmnCode,
                            String vnp_Amount,
                            String vnp_BankCode,
                            String vnp_BankTranNo,
                            String vnp_CardType,
                            String vnp_PayDate,
                            String vnp_OrderInfo,
                            String vnp_TransactionNo,
                            String vnp_ResponseCode,
                            String vnp_TransactionStatus,
                            String vnp_TxnRef,
                            String vnp_SecureHash) throws IOException {
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_TmnCode", URLEncoder.encode(vnp_TmnCode, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_Amount", URLEncoder.encode(vnp_Amount, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_BankCode", URLEncoder.encode(vnp_BankCode, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_BankTranNo", URLEncoder.encode(vnp_BankTranNo, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_CardType", URLEncoder.encode(vnp_CardType, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_PayDate", URLEncoder.encode(vnp_PayDate, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_OrderInfo", URLEncoder.encode(vnp_OrderInfo, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_TransactionNo", URLEncoder.encode(vnp_TransactionNo, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_ResponseCode", URLEncoder.encode(vnp_ResponseCode, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_TransactionStatus", URLEncoder.encode(vnp_TransactionStatus, StandardCharsets.US_ASCII.toString()));
        fields.put("vnp_TxnRef", URLEncoder.encode(vnp_TxnRef, StandardCharsets.US_ASCII.toString()));

        String signValue = PaymentConfig.hashAllFields(fields);
        ResponseObject responseObject = new ResponseObject();
        Order order = orderRepository.findById(Long.valueOf(vnp_TxnRef)).get();
        PaymentResultsDTO paymentResultsDTO = new PaymentResultsDTO();
        paymentResultsDTO.setAmount(vnp_Amount);
        paymentResultsDTO.setBankCode(vnp_BankCode);
        paymentResultsDTO.setBankTranNo(vnp_BankTranNo);
        paymentResultsDTO.setOrderInfo(vnp_OrderInfo);
        paymentResultsDTO.setPayDate(vnp_PayDate);
        paymentResultsDTO.setResponseCode(vnp_ResponseCode);
        paymentResultsDTO.setTransactionNo(vnp_TransactionNo);
        paymentResultsDTO.setTransactionStatus(vnp_TransactionStatus);
        paymentResultsDTO.setTxnRef(vnp_TxnRef);
        if (signValue.equals(vnp_SecureHash)) {
            if (vnp_ResponseCode.equalsIgnoreCase("00") && vnp_TransactionStatus.equalsIgnoreCase("00")) {
                order.setStatus(OrderStatus.PAID);
                order = orderRepository.save(order);
                paymentResultsDTO.setOrder(order);
                responseObject.setStatus("Ok");
                responseObject.setMessage("Payment Success");
                responseObject.setData(paymentResultsDTO);
            }else {
                paymentResultsDTO.setOrder(order);
                responseObject.setStatus("Error");
                responseObject.setMessage("Error");
                responseObject.setData(paymentResultsDTO);
            }
            return responseObject;
        }
        responseObject.setStatus("Error");
        responseObject.setMessage("Sign not valid!");
        responseObject.setData(null);
        return responseObject;
    }
    @Override
    public boolean refund(Order order) throws IOException {
        String vnp_RequestId = PaymentConfig.getRandomNumber(8);
        String vnp_Version = "2.1.0";
        String vnp_Command = "refund";
        String vnp_TmnCode = PaymentConfig.vnp_TmnCode;
        String vnp_TransactionType = "02"; // trả toàn bộ tiền
        String vnp_TxnRef = String.valueOf(order.getId());
        long amount = (long) (order.getAmount() * 100);
        String vnp_Amount = String.valueOf(amount);
        String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;
        String vnp_TransactionNo = ""; //Assuming value of the parameter "vnp_TransactionNo" does not exist on your system.
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String vnp_TransactionDate = dtf.format(order.getCreatedAt());
        User user = order.getUser();
        String vnp_CreateBy;
        if (user == null) {
            vnp_CreateBy = "admin";
        }else {
            vnp_CreateBy = user.getUsername();
        }

        String vnp_CreateDate = formatter.format(cld.getTime());

        String vnp_IpAddr = "192.168.0.1";

        JsonObject vnp_Params = new JsonObject();

        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
        vnp_Params.addProperty("vnp_Version", vnp_Version);
        vnp_Params.addProperty("vnp_Command", vnp_Command);
        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.addProperty("vnp_TransactionType", vnp_TransactionType);
        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.addProperty("vnp_Amount", vnp_Amount);
        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);

        if (vnp_TransactionNo != null && !vnp_TransactionNo.isEmpty()) {
            vnp_Params.addProperty("vnp_TransactionNo", "{get value of vnp_TransactionNo}");
        }

        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransactionDate);
        vnp_Params.addProperty("vnp_CreateBy", vnp_CreateBy);
        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);

        String hash_Data = vnp_RequestId + "|" + vnp_Version + "|" + vnp_Command + "|" + vnp_TmnCode + "|" +
                vnp_TransactionType + "|" + vnp_TxnRef + "|" + vnp_Amount + "|" + vnp_TransactionNo + "|"
                + vnp_TransactionDate + "|" + vnp_CreateBy + "|" + vnp_CreateDate + "|" + vnp_IpAddr + "|" + vnp_OrderInfo;

        String vnp_SecureHash = PaymentConfig.hmacSHA512(PaymentConfig.vnp_HashSecret, hash_Data.toString());

        vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

        URL url = new URL(PaymentConfig.vnp_apiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(vnp_Params.toString());
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();
        String responseString = response.toString();
// Parse the string into a JSON object using Gson
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseString, JsonObject.class);
        String vnp_ResponseCode=jsonObject.get("vnp_ResponseCode").getAsString();
        return vnp_ResponseCode.equals("00");
    }
}

