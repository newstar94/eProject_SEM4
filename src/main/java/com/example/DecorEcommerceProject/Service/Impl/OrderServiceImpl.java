package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.DTO.GhnDTO;
import com.example.DecorEcommerceProject.Entities.DTO.ItemGHN;
import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.example.DecorEcommerceProject.Repositories.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

import static com.example.DecorEcommerceProject.Service.Impl.OrderServiceImpl.GhnApiHandler.*;

@Service
public class OrderServiceImpl implements IOrderService {
    private final VoucherRepository voucherRepository;
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentServiceImpl paymentService;
    private final UserRepository userRepository;
    private final VoucherServiceImpl voucherService;

    public OrderServiceImpl(VoucherRepository voucherRepository, ProductRepository productRepository, DiscountRepository discountRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentServiceImpl paymentService, UserRepository userRepository, VoucherServiceImpl voucherService) {
        this.voucherRepository = voucherRepository;
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.voucherService = voucherService;
    }

    @Override
    @Transactional
    public Object createOrder(OrderDTO orderDTO) throws Exception {
        Order order = new Order();
        order.setStatus(OrderStatus.WAITING);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setCreatedAt(LocalDateTime.now());
        order.setUser(orderDTO.getUser());
        order.setShippingAddress(orderDTO.getShippingAddress());
        String receiverInfo = "Name: " + orderDTO.getShippingAddress().getName()
                + "; Phone: " + orderDTO.getShippingAddress().getPhone()
                + "; Address: " + orderDTO.getShippingAddress().getAddress()
                + "," + orderDTO.getShippingAddress().getWard()
                + "," + orderDTO.getShippingAddress().getDistrict()
                + "," + orderDTO.getShippingAddress().getProvince();
        order.setReceiverInfo(receiverInfo);
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
        int weight = 0;
        int deliveryFee = 0;
        int voucher_discount = 0;
        boolean deliveryAvailable = true;
        for (OrderItem orderItem : orderItems) {
            amount += orderItem.getPrice() * orderItem.getQuantity();
            weight += orderItem.getProduct().getWeight();
            if (!orderItem.getProduct().isDeliveryAvailable()) {
                deliveryAvailable = false;
            }
        }

        if (orderDTO.getVoucherCode() != null) {
            if (!voucherService.checkVoucherIsUsed(orderDTO.getVoucherCode(), orderDTO.getUser().getUsername())) {
                Voucher voucher = voucherRepository.findByCode(orderDTO.getVoucherCode());
                voucher_discount = (int) Math.min(amount - (amount * voucher.getPercentage() / 100), voucher.getAmountMax());
                order.setVoucher_discount(voucher_discount);
                order.setVoucher(voucher);
            }
        }

        if (amount - voucher_discount >= 10000000) {
            order.setDeliveryFee(0);
        } else if (deliveryAvailable) {
            RawData rawData = new RawData();
            rawData.setService_id(53320);
            rawData.setInsurance_value((int) Math.min(amount, 5000000));
            rawData.setHeight(20);
            rawData.setLength(50);
            rawData.setWidth(30);
            rawData.setWeight(weight + 200);
            rawData.setCod_value((int) amount);
            rawData.setTo_ward_code("1A0608"); //test
            rawData.setTo_district_id(1485); //test
//            rawData.setTo_district_id(orderDTO.getShippingAddress().getDistrict_id());
//            rawData.setTo_ward_code(orderDTO.getShippingAddress().getWardCode());

            Gson gson = new Gson();
            String jsonString = gson.toJson(rawData);

            deliveryFee = getDeliveryFee(jsonString);
            order.setDeliveryFee(deliveryFee);
        } else {
            order.setDeliveryFee(deliveryFee);
        }
        order.setAmount(amount);
        order.setTotal((int) (amount - voucher_discount + deliveryFee));
        order = orderRepository.save(order);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }
        return paymentService.createPayment(order.getId());
    }

    @Override
    public Order confirmOrder(Long id) {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.WAITING && existOrder.getPaymentType() == PaymentType.COD) {
            existOrder.setStatus(OrderStatus.CONFIRM);
            return orderRepository.save(existOrder);
        }
        return null;
    }

    @Override
    public Order deliveringOrder(Long id) throws Exception {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.PAID || existOrder.getStatus() == OrderStatus.CONFIRM) {
            GhnDTO ghnDTO = new GhnDTO();
            ghnDTO.setTo_name(existOrder.getShippingAddress().getName());
            ghnDTO.setTo_phone(existOrder.getShippingAddress().getPhone());
            ghnDTO.setTo_address(existOrder.getShippingAddress().getAddress());
            ghnDTO.setTo_ward_code(existOrder.getShippingAddress().getWardCode());
            ghnDTO.setTo_district_id(existOrder.getShippingAddress().getDistrict_id());
            ghnDTO.setCod_amount((int) existOrder.getAmount());
            ghnDTO.setInsurance_value((int) Math.min(existOrder.getAmount(), 5000000));
            ghnDTO.setService_id(53320);
            ghnDTO.setPayment_type_id(1);

            ghnDTO.setRequired_note("CHOXEMHANGKHONGTHU");
            List<ItemGHN> items = new ArrayList<>();
            int weight = 0;
            for (OrderItem item : existOrder.getOrderItems()) {
                ItemGHN itemGHN = new ItemGHN();
                itemGHN.setName(item.getProduct().getName());
                itemGHN.setQuantity(itemGHN.getQuantity());
                items.add(itemGHN);
                weight += item.getProduct().getWeight();
            }
            ghnDTO.setWeight(weight + 200);
            ghnDTO.setHeight(20);
            ghnDTO.setLength(50);
            ghnDTO.setWidth(30);
            ghnDTO.setItems(items);
            Gson gson = new Gson();
            String jsonString = gson.toJson(ghnDTO);
            if (!createGhn(jsonString).isEmpty()) {
                String receiverInfo = "Name: " + existOrder.getShippingAddress().getName()
                        + "; Phone: " + existOrder.getShippingAddress().getPhone()
                        + "; Address: " + existOrder.getShippingAddress().getAddress()
                        + "," + existOrder.getShippingAddress().getWard()
                        + "," + existOrder.getShippingAddress().getDistrict()
                        + "," + existOrder.getShippingAddress().getProvince();
                existOrder.setReceiverInfo(receiverInfo);
                existOrder.setGhnCode(createGhn(jsonString));
                existOrder.setStatus(OrderStatus.DELIVERING);
                return orderRepository.save(existOrder);
            }
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
            if (user != null) {
                user.setPoint((int) (user.getPoint() + existOrder.getAmount() / 1000));
                if (user.getPoint() > 1000) {
                    user.setLevel(Level.SILVER);
                }
                if (user.getPoint() > 10000) {
                    user.setLevel(Level.GOLD);
                }
                if (user.getPoint() > 100000) {
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

    @Data
    public static class RawData {
        private int service_id;
        private int insurance_value;
        private String to_ward_code;
        private int to_district_id;
        private int height;
        private int length;
        private int width;
        private int weight;
        private int cod_value;
    }

    public static class GhnApiHandler {
        private static final String API_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order";

        private static CloseableHttpClient createHttpClient() {
            return HttpClients.createDefault();
        }

        private static JsonObject sendRequest(String apiUrl, String jsonString) throws Exception {
            CloseableHttpClient httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("Token", "6ab4fe69-fe4e-11ed-b678-22ca76951087");
            httpPost.setHeader("shop_id", "124400");

            StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity);

            httpClient.close();

            Gson gson = new Gson();
            return gson.fromJson(responseString, JsonObject.class);
        }

        public static int getDeliveryFee(String jsonString) throws Exception {
            JsonObject jsonObject = sendRequest(API_URL + "/fee", jsonString);
            return jsonObject.getAsJsonObject("data").getAsJsonPrimitive("total").getAsInt();
        }

        public static String createGhn(String jsonString) throws Exception {
            JsonObject jsonObject = sendRequest(API_URL + "/create", jsonString);
            return jsonObject.getAsJsonObject("data").getAsJsonPrimitive("order_code").getAsString();
        }
    }
}

