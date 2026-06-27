package com.makeupseven.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class KitItemResponse {
    private UUID id;
    private String name;
    private String brand;
    private String category;
    private Integer quantity;
    private LocalDate expiryDate;
    private Boolean lowStockAlert;
}
