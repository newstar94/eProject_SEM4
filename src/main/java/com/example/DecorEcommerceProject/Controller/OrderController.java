package com.example.DecorEcommerceProject.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.example.DecorEcommerceProject.Entities.Order;
import com.example.DecorEcommerceProject.Entities.DTO.OrderDTO;
import com.example.DecorEcommerceProject.Service.IOrderService;
import com.example.DecorEcommerceProject.Service.IPaymentService;
import javax.persistence.EntityNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final IOrderService orderService;
    private final IPaymentService paymentService;

    public OrderController(IOrderService orderService, IPaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrder() {
        if (orderService.getAllOrder().size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List order is empty!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getAllOrder());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getAllOrderByUseId(@PathVariable Long id) {
        if (orderService.getAllOrderByUseId(id).size() == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("List order is empty!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getAllOrderByUseId(id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@Validated @RequestBody OrderDTO orderDTO) {
        try {
            Object order = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/result")
    public ResponseEntity<?> result(
            @RequestParam(value = "vnp_TmnCode") String vnp_TmnCode,
            @RequestParam(value = "vnp_Amount") String vnp_Amount,
            @RequestParam(value = "vnp_BankCode") String vnp_BankCode,
            @RequestParam(value = "vnp_BankTranNo") String vnp_BankTranNo,
            @RequestParam(value = "vnp_CardType") String vnp_CardType,
            @RequestParam(value = "vnp_PayDate") String vnp_PayDate,
            @RequestParam(value = "vnp_OrderInfo") String vnp_OrderInfo,
            @RequestParam(value = "vnp_TransactionNo") String vnp_TransactionNo,
            @RequestParam(value = "vnp_ResponseCode") String vnp_ResponseCode,
            @RequestParam(value = "vnp_TransactionStatus") String vnp_TransactionStatus,
            @RequestParam(value = "vnp_TxnRef") String vnp_TxnRef,
            @RequestParam(value = "vnp_SecureHash") String vnp_SecureHash
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.getResult(vnp_TmnCode, vnp_Amount, vnp_BankCode, vnp_BankTranNo, vnp_CardType, vnp_PayDate, vnp_OrderInfo, vnp_TransactionNo, vnp_ResponseCode, vnp_TransactionStatus, vnp_TxnRef, vnp_SecureHash));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        if (!orderService.getOrderById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order with id " + id + " is not existed !");
        } else {
            return ResponseEntity.ok().body(orderService.getOrderById(id));
        }
    }
    @PutMapping("/update/{id}") //for admin
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        try {
            Order updatedOrder = orderService.updateOrder(id, order);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not update order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @PutMapping("/cancel/{id}") //for user
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.cancelOrder(id);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not cancel order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/save")
    public ResponseEntity<?> save(
            @RequestParam(value = "vnp_TmnCode") String vnp_TmnCode,
            @RequestParam(value = "vnp_Amount") String vnp_Amount,
            @RequestParam(value = "vnp_BankCode") String vnp_BankCode,
            @RequestParam(value = "vnp_BankTranNo") String vnp_BankTranNo,
            @RequestParam(value = "vnp_CardType") String vnp_CardType,
            @RequestParam(value = "vnp_PayDate") String vnp_PayDate,
            @RequestParam(value = "vnp_OrderInfo") String vnp_OrderInfo,
            @RequestParam(value = "vnp_TransactionNo") String vnp_TransactionNo,
            @RequestParam(value = "vnp_ResponseCode") String vnp_ResponseCode,
            @RequestParam(value = "vnp_TransactionStatus") String vnp_TransactionStatus,
            @RequestParam(value = "vnp_TxnRef") String vnp_TxnRef,
            @RequestParam(value = "vnp_SecureHash") String vnp_SecureHash
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(paymentService.getResult(vnp_TmnCode, vnp_Amount, vnp_BankCode, vnp_BankTranNo, vnp_CardType, vnp_PayDate, vnp_OrderInfo, vnp_TransactionNo, vnp_ResponseCode, vnp_TransactionStatus, vnp_TxnRef, vnp_SecureHash));
    }
}
