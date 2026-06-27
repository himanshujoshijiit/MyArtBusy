package com.makeupseven.dto;

import com.makeupseven.model.enums.Occasion;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PortfolioItemDto {
    private UUID id;
    private String imageUrl;
    private String caption;
    private Occasion occasion;
    private Integer sortOrder;
}
