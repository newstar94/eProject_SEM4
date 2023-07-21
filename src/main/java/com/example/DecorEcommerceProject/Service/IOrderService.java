package com.example.DecorEcommerceProject.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.example.DecorEcommerceProject.Entities.Order;
import com.example.DecorEcommerceProject.Entities.DTO.OrderDTO;
import com.example.DecorEcommerceProject.Entities.Enum.OrderStatus;

import javax.servlet.http.HttpServletRequest;

public interface IOrderService {
    Object placeOrder(OrderDTO orderDTO, HttpServletRequest request) throws Exception;

    Object checkoutOrder(OrderDTO orderDTO) throws Exception;

    Order confirmOrder(Long id) throws Exception;

    Order deliveringOrder(Long id) throws Exception;

    Order cancelOrder(Long id, HttpServletRequest request) throws IOException;

    Order returnOrder(Long id);

    Order acceptReturnOrder(Long id, HttpServletRequest request) throws IOException;

    Order finishOrder(Long id);

    Optional<Order> getOrderById(Long id);

    List<Order> getAllOrder();

    List<Order> getAllOrderByUseId(Long id);

    String printOrder(Long id) throws Exception;
}
