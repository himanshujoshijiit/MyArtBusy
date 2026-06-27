package com.makeupseven.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class KitItemRequest {
    private String name;
    private String brand;
    private String category;
    private Integer quantity;
    private LocalDate expiryDate;
    private Integer minQuantity;
}
