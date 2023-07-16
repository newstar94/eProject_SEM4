package com.example.DecorEcommerceProject.Entities.DTO;

import lombok.Data;

import java.util.List;

@Data
public class GhnDTO {
    private String to_name;
    private String to_phone;
    private String to_address;
    private String to_ward_code;
    private int to_district_id;
    private int cod_amount;
    private int insurance_value;
    private int weight;
    private int length;
    private int width;
    private int height;
    private int service_id;
    private int payment_type_id;
    private String required_note;
    List<ItemGHN> items;
}
