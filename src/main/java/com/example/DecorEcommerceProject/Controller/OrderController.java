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
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

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

    @PostMapping("/place_order") //tiến hành đặt hàng
    public ResponseEntity<?> placeOrder(@Validated @RequestBody OrderDTO orderDTO, HttpServletRequest request) {
        try {
            Object order = orderService.placeOrder(orderDTO, request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/checkout") //trước bước tến hành đặt hàng, để hiện thị thông tin giá
    public ResponseEntity<?> checkoutOrder(@Validated @RequestBody OrderDTO orderDTO) {
        try {
            Object order = orderService.checkoutOrder(orderDTO);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/confirm/{id}") //for admin
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.confirmOrder(id);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not confirm order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/place_order/{id}") //tiến hành thanh toán lại khi thanh toán onl chưa thành công
    public ResponseEntity<?> checkOut(@PathVariable Long id, HttpServletRequest request) {
        try {
            Object order = paymentService.createPayment(id, request);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/result")
    public ResponseEntity<?> result(@RequestBody Map<String, String> request) throws IOException {
        String vnp_TmnCode = request.get("vnp_TmnCode");
        String vnp_Amount = request.get("vnp_Amount");
        String vnp_BankCode = request.get("vnp_BankCode");
        String vnp_BankTranNo = request.get("vnp_BankTranNo");
        String vnp_CardType = request.get("vnp_CardType");
        String vnp_PayDate = request.get("vnp_PayDate");
        String vnp_OrderInfo = request.get("vnp_OrderInfo");
        String vnp_TransactionNo = request.get("vnp_TransactionNo");
        String vnp_ResponseCode = request.get("vnp_ResponseCode");
        String vnp_TransactionStatus = request.get("vnp_TransactionStatus");
        String vnp_TxnRef = request.get("vnp_TxnRef");
        String vnp_SecureHash = request.get("vnp_SecureHash");
        if (vnp_BankTranNo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(paymentService.getResult(vnp_TmnCode, vnp_Amount, vnp_BankCode, vnp_BankTranNo, vnp_CardType, vnp_PayDate, vnp_OrderInfo, vnp_TransactionNo, vnp_ResponseCode, vnp_TransactionStatus, vnp_TxnRef, vnp_SecureHash));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(paymentService.getResult(vnp_TmnCode, vnp_Amount, vnp_BankCode, "", vnp_CardType, vnp_PayDate, vnp_OrderInfo, vnp_TransactionNo, vnp_ResponseCode, vnp_TransactionStatus, vnp_TxnRef, vnp_SecureHash));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        if (!orderService.getOrderById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order with id " + id + " is not existed !");
        } else {
            return ResponseEntity.ok().body(orderService.getOrderById(id));
        }
    }

    @PutMapping("/delivering/{id}") //for admin
    public ResponseEntity<?> deliveringOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.deliveringOrder(id);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not delivering order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/print/{id}") //for admin
    public ResponseEntity<?> printOrder(@PathVariable Long id) throws Exception {
        String printUrl = orderService.printOrder(id);
        if (printUrl != null) {
            return ResponseEntity.ok(printUrl);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not print!");
    }

    @PutMapping("/cancel/{id}") //for user
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            Order updatedOrder = orderService.cancelOrder(id, request);
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

    @PutMapping("/return/{id}") //for user
    public ResponseEntity<?> returnOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.returnOrder(id);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not return order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/delivered/{id}") //for admin
    public ResponseEntity<?> deliveredOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.deliveredOrder(id);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not finish order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/finish/{id}") //for user
    public ResponseEntity<?> finishOrder(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.finishOrder(id);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not finish order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/accept_return/{id}") //for admin
    public ResponseEntity<?> acceptReturnOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            Order updatedOrder = orderService.acceptReturnOrder(id, request);
            if (updatedOrder != null) {
                return ResponseEntity.ok().body(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Can not accept return order with id: " + id);
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
