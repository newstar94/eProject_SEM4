package com.example.DecorEcommerceProject.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.example.DecorEcommerceProject.Entities.Order;
import com.example.DecorEcommerceProject.Entities.DTO.OrderDTO;
import com.example.DecorEcommerceProject.Entities.Enum.OrderStatus;

public interface IOrderService {
    Object createOrder(OrderDTO orderDTO) throws Exception;
    Order updateOrder(Long id, Order order) throws IOException;
    Order cancelOrder(Long id) throws IOException;
    Optional<Order> getOrderById(Long id);
    List<Order> getAllOrder();
    List<Order> getAllOrderByUseId(Long id);
}
