package org.example.jaego.Dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InventoryDetailDto {
    private Long inventoryId;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Integer totalQuantity;
    private List<StockBatchDto> stockBatches;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
