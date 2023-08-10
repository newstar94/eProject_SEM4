package com.example.DecorEcommerceProject.Service.Impl;

import com.example.DecorEcommerceProject.Entities.DTO.GhnDTO;
import com.example.DecorEcommerceProject.Entities.DTO.ItemGHN;
import com.example.DecorEcommerceProject.Entities.Enum.*;
import com.example.DecorEcommerceProject.Repositories.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import org.springframework.context.ApplicationContextException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.DecorEcommerceProject.Entities.*;
import com.example.DecorEcommerceProject.Entities.DTO.OrderDTO;
import com.example.DecorEcommerceProject.Entities.DTO.OrderItemDTO;
import com.example.DecorEcommerceProject.Service.IOrderService;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class OrderServiceImpl implements IOrderService {
    private final EmailServiceImpl emailService;
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
    private final DeliveryAddressServiceImpl deliveryAddressService;
    private final DeliveryAddressRepository deliveryAddressRepository;

    public OrderServiceImpl(EmailServiceImpl emailService, AdminConfigRepository adminConfigRepository, VoucherRepository voucherRepository, VoucherUserRepository voucherUserRepository, ProductRepository productRepository, DiscountRepository discountRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, PaymentServiceImpl paymentService, UserRepository userRepository, VoucherServiceImpl voucherService, DeliveryAddressServiceImpl deliveryAddressService, DeliveryAddressRepository deliveryAddressRepository) {
        this.emailService = emailService;
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
        this.deliveryAddressService = deliveryAddressService;
        this.deliveryAddressRepository = deliveryAddressRepository;
    }

    @Override
    @Transactional
    public Object placeOrder(OrderDTO orderDTO, HttpServletRequest request) throws Exception {
        AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
        MapApiHandler mapApiHandler = new MapApiHandler(adminConfigRepository);
        Order order = new Order();
        order.setStatus(OrderStatus.WAITING);
        order.setPaymentType(orderDTO.getPaymentType());
        order.setCreatedAt(LocalDateTime.now());
        order.setUser(userRepository.findById(orderDTO.getUser().getId()).get());
        setDeliveryAddress(orderDTO, order);
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO orderItemDTO : orderDTO.getOrderItemDTOS()) {
            OrderItem orderItem = new OrderItem();
            Product product = productRepository.findById(orderItemDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Not found product with id: " + orderItemDTO.getProductId()));
            orderItem.setProduct(product);
            List<DiscountHistory> discountHistories = product.getDiscountHistories();
            if (discountHistories.isEmpty()) {
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
            if (inventory == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
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

        if (orderDTO.getVoucherCode() != null && !orderDTO.getVoucherCode().isEmpty()) {
            if (voucherService.checkVoucherAvailable(orderDTO.getVoucherCode(), order.getUser().getUsername())) {
                Voucher voucher = voucherRepository.findByCode(orderDTO.getVoucherCode());
                voucher_discount = (int) Math.min((amount * voucher.getPercentage() / 100), voucher.getAmountMax());
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
        deliveryFee(orderDTO, adminConfig, ghnApiHandler, mapApiHandler, order, amount, weight, voucher_discount, deliveryAvailable);
        order = orderRepository.save(order);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        }
        order.setCode(generateOrderCode(order.getId()));
        order = orderRepository.save(order);
        return paymentService.createPayment(order.getId(), request);
    }

    private static String generateOrderCode(long id) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        code.append(id);
        code.append(characters.charAt(random.nextInt(characters.length())-10));
        for (int i = 0; i < 8- String.valueOf(id).length(); i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }

    @Override
    public Object checkoutOrder(OrderDTO orderDTO) throws Exception {
        AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
        MapApiHandler mapApiHandler = new MapApiHandler(adminConfigRepository);
        Order order = new Order();
        order.setUser(userRepository.findById(orderDTO.getUser().getId()).get());
        setDeliveryAddress(orderDTO, order);
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO orderItemDTO : orderDTO.getOrderItemDTOS()) {
            OrderItem orderItem = new OrderItem();
            Product product = productRepository.findById(orderItemDTO.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Not found product with id: " + orderItemDTO.getProductId()));
            orderItem.setProduct(product);
            List<DiscountHistory> discountHistories = product.getDiscountHistories();
            if (discountHistories.isEmpty()) {
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

        if (orderDTO.getVoucherCode() != null && !orderDTO.getVoucherCode().isEmpty()) {
            if (voucherService.checkVoucherAvailable(orderDTO.getVoucherCode(), order.getUser().getUsername())) {
                Voucher voucher = voucherRepository.findByCode(orderDTO.getVoucherCode());
                voucher_discount = (int) Math.min((amount * voucher.getPercentage() / 100), voucher.getAmountMax());
                order.setVoucher_discount(voucher_discount);
            } else {
                throw new ApplicationContextException("Voucher không hợp lệ hoặc đã hết lượt sử dụng!");
            }
        }
        //Tính phí vận chuyển
        deliveryFee(orderDTO, adminConfig, ghnApiHandler, mapApiHandler, order, amount, weight, voucher_discount, deliveryAvailable);
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);
        }
        return order;
    }

    private void setDeliveryAddress(OrderDTO orderDTO, Order order) {
        if (orderDTO.getDeliveryAddress().getId() == null) {
            DeliveryAddress deliveryAddress = deliveryAddressService.createDeliveryAddress(orderDTO.getDeliveryAddress());
            order.setDeliveryAddress(deliveryAddress);
        } else {
            order.setDeliveryAddress(deliveryAddressRepository.findById(orderDTO.getDeliveryAddress().getId()).get());
        }
    }

    private void deliveryFee(OrderDTO orderDTO, AdminConfig adminConfig, GhnApiHandler ghnApiHandler, MapApiHandler mapApiHandler, Order order, double amount, int weight, int voucher_discount, boolean deliveryAvailable) throws Exception {
        int deliveryFee;
        if (orderDTO.getDeliveryType() == DeliveryType.SHOP || !deliveryAvailable) {
            String receiveAdd = order.getDeliveryAddress().getAddress()
                    + "," + order.getDeliveryAddress().getWard()
                    + "," + order.getDeliveryAddress().getDistrict()
                    + "," + order.getDeliveryAddress().getProvince();
            int distance = mapApiHandler.calculateDistance(receiveAdd);
            if (distance <= adminConfig.getMax_distance()) {
                deliveryFee = adminConfig.getDelivery_fee();
            } else {
                deliveryFee = distance * adminConfig.getDelivery_fee_km();
            }
            order.setDeliveryType(DeliveryType.SHOP);
        } else {        //tính phí vận chuyển GHN
            RawData rawData = new RawData();
            rawData.setService_id(53320);
            rawData.setInsurance_value((int) Math.min(amount, 5000000));
            rawData.setHeight(20);
            rawData.setLength(50);
            rawData.setWidth(30);
            rawData.setWeight(weight + 200);
            if (orderDTO.getPaymentType() == PaymentType.COD) {
                rawData.setCod_value((int) amount);
            } else {
                rawData.setCod_value(0);
            }
            rawData.setTo_district_id(deliveryAddressRepository.findById(orderDTO.getDeliveryAddress().getId()).get().getDistrict_id());
            rawData.setTo_ward_code(deliveryAddressRepository.findById(orderDTO.getDeliveryAddress().getId()).get().getWardCode());
            order.setDeliveryType(DeliveryType.GHN);

            Gson gson = new Gson();
            String jsonString = gson.toJson(rawData);
            try {
                deliveryFee = ghnApiHandler.getDeliveryFee(jsonString);
            } catch (Exception e) {
                throw new ApplicationContextException("Can calculate delivery fee");
            }

        }
        if (amount - voucher_discount >= adminConfig.getAmount_to_free()) {
            deliveryFee = 0;
            order.setDeliveryFee(deliveryFee);
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
    public String printOrder(Long id) throws Exception {
        AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        List<String> order_codes = new ArrayList<>();
        if (existOrder.getStatus() == OrderStatus.DELIVERING) {
            if (existOrder.getDeliveryType() == DeliveryType.GHN) {
                order_codes.add(existOrder.getGhnCode());
                Gson gson = new Gson();
                String jsonString = gson.toJson(new print(order_codes));
                return "https://dev-online-gateway.ghn.vn/a5/public-api/printA5?token=" + ghnApiHandler.printGhn(jsonString);
            } else {
                String path = "D:\\" + existOrder.getId() + ".pdf";
                try {
                    PdfWriter pdfWriter = new PdfWriter(path);
                    PdfDocument pdfDoc = new PdfDocument(pdfWriter);
                    pdfDoc.setDefaultPageSize(PageSize.A5);
                    Document doc = new Document(pdfDoc);
                    PdfFont font = PdfFontFactory.createFont("c:/windows/fonts/arial.ttf", "Identity-H", true);
                    doc.setFont(font);
                    Div spacingDiv = new Div().setHeight(8f);

                    Table sellerTable = new Table(UnitValue.createPercentArray(new float[]{1}));
                    Paragraph nameParagraph = new Paragraph(adminConfig.getName()).setFontSize(12).setBold();
                    Paragraph addressParagraph = new Paragraph(adminConfig.getAddress()).setFontSize(12).setBold();
                    Cell sellerCell = new Cell()
                            .add("Bên gửi").setFontSize(10)
                            .add(nameParagraph)
                            .add(addressParagraph);
                    sellerTable.addCell(sellerCell);

                    Table buyerTable = new Table(UnitValue.createPercentArray(new float[]{1}));
                    DeliveryAddress deliveryAddress = existOrder.getDeliveryAddress();
                    Paragraph buyerInfo = new Paragraph(deliveryAddress.getName() + " - "
                            + deliveryAddress.getPhone())
                            .setFontSize(12).setBold();
                    Paragraph buyerAddress = new Paragraph(deliveryAddress.getAddress() + ", "
                            + deliveryAddress.getWard() + ", "
                            + deliveryAddress.getDistrict() + ", "
                            + deliveryAddress.getProvince())
                            .setFontSize(12).setBold();
                    Paragraph codValue = new Paragraph();
                    if (existOrder.getPaymentType() == PaymentType.COD) {
                        Locale locale = new Locale("vi", "VN");
                        // Get a number formatter for the specified locale
                        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
                        // Format the number using the number formatter
                        String formattedNumber = numberFormat.format(existOrder.getTotal());
                        codValue.add(formattedNumber).setBold().setFontSize(12);
                    } else {
                        codValue.add("0").setBold().setFontSize(12);
                    }
                    Cell buyerCell = new Cell()
                            .add("Bên nhận").setFontSize(10)
                            .add(buyerInfo)
                            .add(buyerAddress)
                            .add("Thu tiền người nhận")
                            .add(codValue);
                    buyerTable.addCell(buyerCell);

                    Table infoTable = new Table((UnitValue.createPercentArray(new float[]{80, 20})));

                    infoTable.addCell(new Cell().add("Nội dung đơn hàng")
                            .setTextAlignment(TextAlignment.CENTER));
                    int quantity = 0;
                    for (OrderItem orderItem : existOrder.getOrderItems()) {
                        quantity += orderItem.getQuantity();
                    }
                    Paragraph quantityParagraph = new Paragraph()
                            .add("SL tổng: ")
                            .add(new Text(String.valueOf(quantity)).setBold());
                    infoTable.addCell(new Cell().add(quantityParagraph));
                    for (OrderItem orderItem : existOrder.getOrderItems()) {
                        infoTable.addCell(new Cell().add(orderItem.getProduct().getName()));
                        infoTable.addCell(new Cell().add(orderItem.getQuantity().toString())
                                .setTextAlignment(TextAlignment.CENTER));
                    }

                    float remainingHeight = PageSize.A5.getHeight() - doc.getRenderer().getCurrentArea().getBBox().getHeight();
                    Cell newCell = new Cell(0, 2).add("");
                    newCell.setHeight(remainingHeight);
                    infoTable.addCell(newCell);

                    doc.add(sellerTable)
                            .add(spacingDiv)
                            .add(buyerTable)
                            .add(spacingDiv)
                            .add(infoTable);
                    doc.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return path;
            }
        }
        return null;
    }

    @Data
    public static class print {
        private List<String> order_codes;

        public print(List<String> order_codes) {
            this.order_codes = order_codes;
        }
    }

    @Override
    public Order deliveringOrder(Long id) throws Exception {
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getDeliveryType() == DeliveryType.GHN) {
            if (existOrder.getStatus() == OrderStatus.PAID || existOrder.getStatus() == OrderStatus.CONFIRM) {
                GhnDTO ghnDTO = new GhnDTO();
                ghnDTO.setTo_name(existOrder.getDeliveryAddress().getName());
                ghnDTO.setTo_phone(existOrder.getDeliveryAddress().getPhone());
                ghnDTO.setTo_address(existOrder.getDeliveryAddress().getAddress());
                ghnDTO.setTo_ward_code(existOrder.getDeliveryAddress().getWardCode());
                ghnDTO.setTo_district_id(existOrder.getDeliveryAddress().getDistrict_id());
                if (existOrder.getPaymentType() == PaymentType.ONLINE) {
                    ghnDTO.setCod_amount(0);
                } else {
                    ghnDTO.setCod_amount(existOrder.getTotal());
                }
                ghnDTO.setInsurance_value((int) Math.min(existOrder.getAmount(), 5000000));
                ghnDTO.setService_id(53320);
                ghnDTO.setPayment_type_id(1); //1: người gửi trả phí; 2: người nhận trả phí
                ghnDTO.setRequired_note("CHOXEMHANGKHONGTHU");
                List<ItemGHN> items = new ArrayList<>();
                int weight = 0;
                for (OrderItem item : existOrder.getOrderItems()) {
                    ItemGHN itemGHN = new ItemGHN();
                    itemGHN.setName(item.getProduct().getName());
                    itemGHN.setQuantity(item.getQuantity());
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
                String ghnCode = ghnApiHandler.createGhn(jsonString);
                if (!ghnCode.isEmpty()) {
                    existOrder.setGhnCode(ghnCode);
                    existOrder.setStatus(OrderStatus.DELIVERING);
                    String to = existOrder.getUser().getEmail();
                    String subject = "Đơn hàng của bạn đang được vận chuyển";
                    String content = "<h2>Xin chào " + existOrder.getUser().getName() + "</h2>" +
                            "<p>Đơn hàng " + existOrder.getId() + " của bạn đang được vận chuyển</p>" +
                            "<p>Mã đơn hàng GHN: " + existOrder.getGhnCode() + " </p>";
                    emailService.sendEmail(to, subject, content);
                    return orderRepository.save(existOrder);
                }
            }
        } else {
            existOrder.setStatus(OrderStatus.DELIVERING);
            String to = existOrder.getUser().getEmail();
            String subject = "Đơn hàng của bạn đang được vận chuyển";
            String content = "<h2>Xin chào " + existOrder.getUser().getName() + "</h2>" +
                    "<p>Đơn hàng " + existOrder.getId() + " của bạn đang được vận chuyển</p>";
            emailService.sendEmail(to, subject, content);
            return orderRepository.save(existOrder);
        }
        return null;
    }

    @Override
    public Order deliveredOrder(Long id) {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.DELIVERING) {
            existOrder.setStatus(OrderStatus.DELIVERED);
            existOrder.setReceivedAt(LocalDateTime.now());
            return orderRepository.save(existOrder);
        }
        return null;
    }

    @Override
    @Transactional
    public Order cancelOrder(Long id, HttpServletRequest request) throws IOException {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.PAID || existOrder.getStatus() == OrderStatus.WAITING || existOrder.getStatus() == OrderStatus.CONFIRM) {
            if (existOrder.getPaymentType() == PaymentType.COD) {
                return cancelOrder(existOrder);
            }
            if (existOrder.getPaymentType() == PaymentType.ONLINE && existOrder.getStatus() == OrderStatus.WAITING) {
                return cancelOrder(existOrder);
            }
            if (paymentService.refund(existOrder, request)) {
                return cancelOrder(existOrder);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Order acceptReturnOrder(Long id, HttpServletRequest request) throws IOException {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.RETURN) {
            if (existOrder.getPaymentType() == PaymentType.COD) {
                return cancelOrder(existOrder);
            }
            if (existOrder.getPaymentType() == PaymentType.ONLINE) {
                if (paymentService.refund(existOrder, request)) {
                    return cancelOrder(existOrder);
                }
            }
        }
        return null;
    }

    private Order cancelOrder(Order existOrder) {
        if (existOrder.getStatus() == OrderStatus.RETURN) {
            existOrder.setStatus(OrderStatus.RETURNED);
        } else {
            existOrder.setStatus(OrderStatus.CANCELLED);
        }
        for (OrderItem orderItem : existOrder.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setInventory(product.getInventory() + orderItem.getQuantity());
            product.setStatus(ProductStatus.AVAILABLE);
            productRepository.save(product);
            DiscountHistory discountHistory = orderItem.getDiscountHistory();
            if (discountHistory != null) {
                Discount discount = discountHistory.getDiscount();
                discount.setLimit(discount.getLimit() + orderItem.getQuantity());
                discountRepository.save(discount);
            }
        }
        Voucher voucher = existOrder.getVoucher();
        if (voucher != null) {
            voucher.setLimit(voucher.getLimit() + 1);
            VoucherUser voucherUser = voucherUserRepository.findVoucherUserByUserIdAndVoucherId(existOrder.getUser().getId(), voucher.getId());
            voucherUser.setUsed(false);
            voucherRepository.save(voucher);
            voucherUserRepository.save(voucherUser);
        }
        return orderRepository.save(existOrder);
    }

    @Override
    public Order returnOrder(Long id) {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.DELIVERED) {
            existOrder.setStatus(OrderStatus.RETURN);
            return orderRepository.save(existOrder);
        }
        return null;
    }

    @Override
    @Transactional
    public Order finishOrder(Long id) {
        Order existOrder = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Not found order with id: " + id));
        if (existOrder.getStatus() == OrderStatus.DELIVERED) {
            existOrder.setStatus(OrderStatus.FINISHED);
            for (OrderItem orderItem : existOrder.getOrderItems()) {
                Product product = orderItem.getProduct();
                product.setTotal_sold(product.getTotal_sold() + orderItem.getQuantity());
                productRepository.save(product);
            }
            User user = existOrder.getUser();
            if (user != null) {
                user.setPoint((int) (user.getPoint() + existOrder.getAmount() / 1000));
                if (user.getPoint() > 5000) {
                    user.setLevel(Level.SILVER);
                }
                if (user.getPoint() > 20000) {
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

    @Scheduled(cron = "0 0 0 * * ?")
    public void autoFinishOrder() {
        List<Order> list = orderRepository.findAllDeliveredOrder();
        for (Order order : list) {
            if (order.getReceivedAt().isBefore(LocalDateTime.now().minusDays(3))) {
                order.setStatus(OrderStatus.FINISHED);
                orderRepository.save(order);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void autoDeliveredOrder() throws Exception {
        List<Order> list = orderRepository.findAllDeliveringOrder();
        GhnApiHandler ghnApiHandler = new GhnApiHandler(adminConfigRepository);
        for (Order order : list) {
            String ghnCode = order.getGhnCode();
            Gson gson = new Gson();
            String jsonString = gson.toJson(new GhnCode(ghnCode));
            if (ghnApiHandler.status(jsonString).status.equals("delivered")) {
                order.setStatus(OrderStatus.DELIVERED);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                LocalDateTime dateTime = LocalDateTime.parse(ghnApiHandler.status(jsonString).updatedDate, formatter);
                order.setReceivedAt(dateTime);
                orderRepository.save(order);
            }
        }
    }

    public static class GhnCode {
        public GhnCode(String order_code) {
            this.order_code = order_code;
        }

        private String order_code;
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

        private String printGhnApiUrl() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getGhn_print_url();
        }

        private String statusGhnApiUrl() {
            AdminConfig adminConfig = adminConfigRepository.findFirstByOrderByIdAsc();
            return adminConfig.getGhn_status_url();
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

        public String printGhn(String jsonString) throws Exception {
            JsonObject jsonObject = sendRequest(printGhnApiUrl(), jsonString);
            return jsonObject.getAsJsonObject("data").getAsJsonPrimitive("token").getAsString();
        }

        public Status status(String jsonString) throws Exception {
            JsonObject jsonObject = sendRequest(statusGhnApiUrl(), jsonString);
            Status status = new Status();
            status.setUpdatedDate(jsonObject.getAsJsonObject("data").getAsJsonPrimitive("updated_date").getAsString());
            status.setStatus(jsonObject.getAsJsonObject("data").getAsJsonPrimitive("status").getAsString());
            return status;
        }

        @Data
        public static class Status {
            private String updatedDate;
            private String status;
        }
    }

    public static class MapApiHandler {
        private final AdminConfigRepository adminConfigRepository;

        public MapApiHandler(AdminConfigRepository adminConfigRepository) {
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
                    + "?origins=" + URLEncoder.encode(getCoordinates(getAddress()), "UTF-8")
                    + "&destinations=" + URLEncoder.encode(getCoordinates(receiveAdd), "UTF-8")
                    + "&travelMode=driving&key=" + getMapToken();
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
            double travelDistance = jsonObject
                    .getAsJsonArray("resourceSets").get(0)
                    .getAsJsonObject().getAsJsonArray("resources").get(0)
                    .getAsJsonObject().getAsJsonArray("results").get(0)
                    .getAsJsonObject().get("travelDistance").getAsDouble();
            return (int) Math.round(travelDistance);
        }

        public String getCoordinates(String location) {
            try {
                HttpClient httpClient = HttpClients.createDefault();
                URIBuilder uriBuilder = new URIBuilder("http://dev.virtualearth.net/REST/v1/Locations");

                // Thêm tham số cho yêu cầu API
                uriBuilder.addParameter("q", location);
                uriBuilder.addParameter("key", getMapToken());

                HttpGet httpGet = new HttpGet(uriBuilder.build());
                HttpResponse response = httpClient.execute(httpGet);

                // Xử lý kết quả trả về
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = new JSONObject(responseBody);

                    JSONArray resources = jsonObject.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources");
                    if (resources.length() > 0) {
                        JSONObject point = resources.getJSONObject(0).getJSONObject("point");
                        Double latitude = (Double) point.getJSONArray("coordinates").get(0);
                        Double longitude = (Double) point.getJSONArray("coordinates").get(1);
                        return latitude + "," + longitude;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null; // Trả về null nếu có lỗi hoặc không có kết quả
        }
    }
}

