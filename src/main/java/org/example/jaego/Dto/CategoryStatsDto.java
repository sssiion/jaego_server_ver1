package org.example.jaego.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryStatsDto {
    private Long categoryId;
    private String categoryName;
    private String categoryType;
    private Long inventoryCount;
    private Integer totalQuantity;
    private Long urgentBatchCount;
}
