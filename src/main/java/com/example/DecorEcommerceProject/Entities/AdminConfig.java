package com.example.DecorEcommerceProject.Entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "adminconfig")
@Data
public class AdminConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String vnp_PayUrl;
    private String vnp_ReturnUrl;
    private String vnp_TmnCode;
    private String vnp_HashSecret;
    private String vnp_apiUrl;
    private int delivery_fee_km; //phí vận chuyển cho 1km
    private int amount_to_free; //Giá trị đơn hàng để được miễn phí vận chuyển
    private int max_distance; //khoảng cách áp giá vận chuyển đồng giá (dưới 5km đồng giá 40k)
    private int delivery_fee; //phí vận chuyển đồng giá
    private String ghn_token; //token GHN
    private String shop_id;
    private String ghn_fee_url; //url tính phí GHN
    private String ghn_create_url; //url tạo đơn GHN
    private String ghn_print_url;
    private String ghn_status_url;
    private String map_token; //token GGMap
    private String map_url; //url GGMap
    private String address;
}
