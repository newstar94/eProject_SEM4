package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.DTO.GhnDTO;
import com.example.DecorEcommerceProject.Entities.DTO.ItemGHN;
import com.example.DecorEcommerceProject.Entities.Enum.DeliveryType;
import com.example.DecorEcommerceProject.Entities.Enum.Level;
import com.example.DecorEcommerceProject.Repositories.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements IOrderService {
    private final AdminConfigRepository adminConfigRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUserRepository voucherUserRepository;
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentServiceImpl paymentService;
    private final UserRepository userRepository;
    private final VoucherServiceImpl voucherService;

    public OrderServiceImpl(AdminConfigRepository adminConfigRepository, VoucherRepository voucherRepository, VoucherUserRepository voucherUserRepository, ProductRepository productRepository, DiscountRepository discountRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentServiceImpl paymentService, UserRepository userRepository, VoucherServiceImpl voucherService) {
        this.adminConfigRepository = adminConfigRepository;
        this.voucherRepository = voucherRepository;
        this.voucherUserRepository = voucherUserRepository;
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
    public Object placeOrder(OrderDTO orderDTO) throws Exception {
        AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
        GGMapApiHandler ggMapApiHandler = new GGMapApiHandler(adminConfigRepository);
        Order order = new Order();
        order.setStatus(OrderStatus.WAITING);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setCreatedAt(LocalDateTime.now());
        order.setUser(orderDTO.getUser());
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
        int weight = 0;
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
            if (voucherService.checkVoucherAvailable(orderDTO.getVoucherCode(), orderDTO.getUser().getUsername())) {
                Voucher voucher = voucherRepository.findByCode(orderDTO.getVoucherCode());
                voucher_discount = (int) Math.min(amount - (amount * voucher.getPercentage() / 100), voucher.getAmountMax());
                order.setVoucher_discount(voucher_discount);
                order.setVoucher(voucher);
                voucher.setLimit(voucher.getLimit() - 1);
                voucherRepository.save(voucher);
                VoucherUser voucherUser = voucherUserRepository.findVoucherUserByUserIdAndVoucherId(orderDTO.getUser().getId(), voucher.getId());
                voucherUser.setUsed(true);
                voucherUserRepository.save(voucherUser);
            } else {
                throw new ApplicationContextException("Voucher không hợp lệ hoặc đã hết lượt sử dụng!");
            }
        }
        //Tính phí vận chuyển
        deliveryFee(orderDTO, adminConfig, ghnApiHandler, ggMapApiHandler, order, amount, weight, voucher_discount, deliveryAvailable);
        order = orderRepository.save(order);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }
        return paymentService.createPayment(order.getId());
    }

    @Override
    public Object checkoutOrder(OrderDTO orderDTO) throws Exception {
        AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
        GGMapApiHandler ggMapApiHandler = new GGMapApiHandler(adminConfigRepository);
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
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
                    discount.setLimit(discount.getLimit() - orderItemDTO.getQuantity());
                    int limit = discount.getLimit();
                    if (limit >= 0) {
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
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        double amount = 0;
        int weight = 0;
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
            if (voucherService.checkVoucherAvailable(orderDTO.getVoucherCode(), orderDTO.getUser().getUsername())) {
                Voucher voucher = voucherRepository.findByCode(orderDTO.getVoucherCode());
                voucher_discount = (int) Math.min(amount - (amount * voucher.getPercentage() / 100), voucher.getAmountMax());
                order.setVoucher_discount(voucher_discount);
            } else {
                throw new ApplicationContextException("Voucher không hợp lệ hoặc đã hết lượt sử dụng!");
            }
        }
        //Tính phí vận chuyển
        deliveryFee(orderDTO, adminConfig, ghnApiHandler, ggMapApiHandler, order, amount, weight, voucher_discount, deliveryAvailable);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }
        return order;
    }

    private void deliveryFee(OrderDTO orderDTO, AdminConfig adminConfig, GhnApiHandler ghnApiHandler, GGMapApiHandler ggMapApiHandler, Order order, double amount, int weight, int voucher_discount, boolean deliveryAvailable) throws Exception {
        int deliveryFee;
        if (orderDTO.getDeliveryType() == DeliveryType.SHOP || !deliveryAvailable) {
            String receiveAdd = orderDTO.getShippingAddress().getAddress()
                    + "," + orderDTO.getShippingAddress().getWard()
                    + "," + orderDTO.getShippingAddress().getDistrict()
                    + "," + orderDTO.getShippingAddress().getProvince();
            int distance = ggMapApiHandler.calculateDistance(receiveAdd) / 1000;
            if (distance <= adminConfig.getMax_distance()) {
                deliveryFee = adminConfig.getDelivery_fee();
            } else {
                deliveryFee = distance * adminConfig.getDelivery_fee_km();
            }
        } else {        //tính phí vận chuyển GHN
            RawData rawData = new RawData();
            rawData.setService_id(53320);
            rawData.setInsurance_value((int) Math.min(amount, 5000000));
            rawData.setHeight(20);
            rawData.setLength(50);
            rawData.setWidth(30);
            rawData.setWeight(weight + 200);
            rawData.setCod_value((int) amount);
//            rawData.setTo_ward_code("1A0608"); //test
//            rawData.setTo_district_id(1485); //test
            rawData.setTo_district_id(orderDTO.getShippingAddress().getDistrict_id());
            rawData.setTo_ward_code(orderDTO.getShippingAddress().getWardCode());

            Gson gson = new Gson();
            String jsonString = gson.toJson(rawData);
            deliveryFee = ghnApiHandler.getDeliveryFee(jsonString);
        }
        if (amount - voucher_discount >= adminConfig.getAmount_to_free()) {
            order.setDeliveryFee(0);
        } else {
            order.setDeliveryFee(deliveryFee);
        }
        order.setAmount(amount);
        order.setTotal((int) (amount - voucher_discount + deliveryFee));
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
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
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
            if (!ghnApiHandler.createGhn(jsonString).isEmpty()) {
                existOrder.setGhnCode(ghnApiHandler.createGhn(jsonString));
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
        private final AdminConfigRepository adminConfigRepository;

        public GhnApiHandler(AdminConfigRepository adminConfigRepository) {
            this.adminConfigRepository = adminConfigRepository;
        }

        private String getDeliveryFeeApiUrl() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getGhn_fee_url();
        }

        private String createGhnApiUrl() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getGhn_create_url();
        }

        private String getToken() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getGhn_token();
        }

        private String getShopId() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getShop_id();
        }

        private CloseableHttpClient createHttpClient() {
            return HttpClients.createDefault();
        }

        private JsonObject sendRequest(String apiUrl, String jsonString) throws Exception {
            CloseableHttpClient httpClient = createHttpClient();
            HttpPost httpPost = new HttpPost(apiUrl);
            httpPost.setHeader("Token", getToken());
            httpPost.setHeader("shop_id", getShopId());

            StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity);

            httpClient.close();

            Gson gson = new Gson();
            return gson.fromJson(responseString, JsonObject.class);
        }

        public int getDeliveryFee(String jsonString) throws Exception {
            JsonObject jsonObject = sendRequest(getDeliveryFeeApiUrl(), jsonString);
            return jsonObject.getAsJsonObject("data").getAsJsonPrimitive("total").getAsInt();
        }

        public String createGhn(String jsonString) throws Exception {
            JsonObject jsonObject = sendRequest(createGhnApiUrl(), jsonString);
            return jsonObject.getAsJsonObject("data").getAsJsonPrimitive("order_code").getAsString();
        }
    }

    public static class GGMapApiHandler {
        private final AdminConfigRepository adminConfigRepository;

        public GGMapApiHandler(AdminConfigRepository adminConfigRepository) {
            this.adminConfigRepository = adminConfigRepository;
        }

        private String getMapToken() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getMap_token();
        }

        private String getMapUrl() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getMap_url();
        }

        private String getAddress() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getAddress();
        }

        private CloseableHttpClient createHttpClient() {
            return HttpClients.createDefault();
        }

        private JsonObject sendRequest(String receiveAdd) throws Exception {
            CloseableHttpClient httpClient = createHttpClient();
            String apiUrl = getMapUrl()
                    + "?origins=" + URLEncoder.encode(getAddress(), "UTF-8")
                    + "&destinations=" + URLEncoder.encode(receiveAdd, "UTF-8")
                    + "&units=metric&key=" + getMapToken();
            HttpGet httpGet = new HttpGet(apiUrl);
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity);

            httpClient.close();

            Gson gson = new Gson();
            return gson.fromJson(responseString, JsonObject.class);
        }

        public int calculateDistance(String receiveAdd) throws Exception {
            JsonObject jsonObject = sendRequest(receiveAdd).getAsJsonObject();
            JsonArray rowsArray = jsonObject.getAsJsonArray("rows");
            JsonObject rowObject = rowsArray.get(0).getAsJsonObject();
            JsonArray elementsArray = rowObject.getAsJsonArray("elements");
            JsonObject elementObject = elementsArray.get(0).getAsJsonObject();
            JsonObject distanceObject = elementObject.getAsJsonObject("distance");
            return distanceObject.get("value").getAsInt();
        }
    }
}

