package com.org.orderservice.client.dto;

import lombok.Data;

@Data
public class ProductClientResponse {
    private Long id;
    private String title;
    private Double price;
    private String description;
    private String category;
    private String image;
}
