package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class StockBatchDto {
    private Long id;
    private Long inventoryId;
    private String inventoryName;
    private Integer quantity;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
