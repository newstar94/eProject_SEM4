package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.example.DecorEcommerceProject.Repositories.*;
import org.springframework.context.ApplicationContextException;
import org.springframework.stereotype.Service;
import com.example.DecorEcommerceProject.Entities.*;
import com.example.DecorEcommerceProject.Entities.DTO.OrderDTO;
import com.example.DecorEcommerceProject.Entities.DTO.OrderItemDTO;
import com.example.DecorEcommerceProject.Entities.Enum.OrderStatus;
import com.example.DecorEcommerceProject.Entities.Enum.PaymentType;
import com.example.DecorEcommerceProject.Service.IOrderService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements IOrderService {
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentServiceImpl paymentService;
    private final UserRepository userRepository;

    public OrderServiceImpl(ProductRepository productRepository, DiscountRepository discountRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentServiceImpl paymentService, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Object createOrder(OrderDTO orderDTO) throws Exception {
        Order order = new Order();
        order.setStatus(OrderStatus.WAITING);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setCreatedAt(LocalDateTime.now());
        order.setUser(orderDTO.getUser());
        order.setVoucher(orderDTO.getVoucher());
        order.setShippingAddress(orderDTO.getShippingAddress());
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO orderItemDTO : orderDTO.getOrderItemDTOS()) {
            OrderItem orderItem = new OrderItem();
            Product product = productRepository.findById(orderItemDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Not found product with id: " + orderItemDTO.getProductId()));
            orderItem.setProduct(product);
            List<DiscountHistory> discountHistories = product.getDiscountHistories();
            if (discountHistories.size() == 0) {
                orderItem.setPrice(product.getPrice());
            }
            for (DiscountHistory discountHistory : discountHistories) {
                Discount discount = discountHistory.getDiscount();
                if (discount.getStart().isBefore(LocalDateTime.now())
                        && discount.getEnd().isAfter(LocalDateTime.now()) && discount.getLimit() > 0) {
                    double discountAmount = Math.min(discount.getDiscountAmountMax(),
                            product.getPrice() * discount.getDiscountPercentage() / 100);
                    orderItem.setPrice(product.getPrice() - discountAmount);
                    orderItem.setDiscountHistory(discountHistory);
                    discount.setLimit(discount.getLimit() - orderItemDTO.getQuantity());
                    int limit = discount.getLimit();
                    if (limit >= 0) {
                        discountRepository.save(discount);
                        break;
                    }
                    throw new ApplicationContextException("Not enough discount for product " + product.getName());
                }
                orderItem.setPrice(product.getPrice());
            }
            orderItem.setQuantity(orderItemDTO.getQuantity());
            int inventory = product.getInventory() - orderItemDTO.getQuantity();
            if (inventory < 0) {
                throw new ApplicationContextException("Not enough inventory for product " + product.getName());
            }
            product.setInventory(inventory);
            productRepository.save(product);
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        double amount = 0;
        for (OrderItem orderItem : orderItems) {
            amount += orderItem.getPrice() * orderItem.getQuantity();
        }
        order.setVoucher(orderDTO.getVoucher());
        order.setAmount(amount);
        order = orderRepository.save(order);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }
        return paymentService.createPayment(order.getId());
    }

    @Override
    public Order updateOrder(Long id, Order order) throws IOException {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            if (existOrder.getStatus() == OrderStatus.WAITING) {
                cancelOrder(id);
            }
            if (existOrder.getStatus() == OrderStatus.RETURN) {
                cancelOrder(id);
            }
        }
        if (existOrder.getStatus() == OrderStatus.PAID || existOrder.getStatus() == OrderStatus.WAITING || existOrder.getStatus() == OrderStatus.CONFIRM) {
            existOrder.setStatus(order.getStatus());
            return orderRepository.save(existOrder);
        }
        return null;
    }

    @Override
    @Transactional
    public Order cancelOrder(Long id) throws IOException {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.PAID || existOrder.getStatus() == OrderStatus.WAITING || existOrder.getStatus() == OrderStatus.CONFIRM) {
            if (existOrder.getPaymentType() == PaymentType.COD) {
                return cancelOrder(existOrder);
            }
            if (existOrder.getPaymentType() == PaymentType.ONLINE && existOrder.getStatus() == OrderStatus.WAITING) {
                return cancelOrder(existOrder);
            }
            if (paymentService.refund(existOrder)) {
                return cancelOrder(existOrder);
            }
        }
        return null;
    }

    private Order cancelOrder(Order existOrder) {
        existOrder.setStatus(OrderStatus.CANCELLED);
        for (OrderItem orderItem : existOrder.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setInventory(product.getInventory() + orderItem.getQuantity());
            productRepository.save(product);
            DiscountHistory discountHistory = orderItem.getDiscountHistory();
            if (discountHistory != null) {
                Discount discount = discountHistory.getDiscount();
                discount.setLimit(discount.getLimit() + orderItem.getQuantity());
                discountRepository.save(discount);
            }
        }
        return orderRepository.save(existOrder);
    }

    @Override
    public Order returnOrder(Long id) {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.DELIVERING) {
            existOrder.setStatus(OrderStatus.RETURN);
            return orderRepository.save(existOrder);
        }
        return null;
    }

    @Override
    public Order finishOrder(Long id) {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.DELIVERING) {
            existOrder.setStatus(OrderStatus.FINISHED);
            User user = existOrder.getUser();
            if (user!=null){
                user.setPoint((int) (user.getPoint()+existOrder.getAmount()/1000));
                if (user.getPoint()>1000){
                    user.setLevel(Level.SILVER);
                }
                if (user.getPoint()>10000){
                    user.setLevel(Level.GOLD);
                }
                if (user.getPoint()>100000){
                    user.setLevel(Level.DIAMOND);
                }
                userRepository.save(user);
            }
            return orderRepository.save(existOrder);
        }
        return null;
    }

    @Override
    public Optional<Order> getOrderById(Long Id) {
        return orderRepository.findById(Id);
    }

    @Override
    public List<Order> getAllOrder() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> getAllOrderByUseId(Long id) {
        return orderRepository.findAllByUserId(id);
    }
}

