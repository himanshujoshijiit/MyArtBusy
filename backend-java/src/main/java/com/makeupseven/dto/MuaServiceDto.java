package com.makeupseven.dto;

import com.makeupseven.model.enums.Occasion;
import com.makeupseven.model.enums.ServiceCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MuaServiceDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private Occasion occasion;
    private ServiceCategory category;
}
