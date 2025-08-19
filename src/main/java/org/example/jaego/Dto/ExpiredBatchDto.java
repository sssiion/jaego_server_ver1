package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpiredBatchDto {
    private Long id;
    private Long inventoryId;
    private String inventoryName;
    private String categoryName;
    private Integer quantity;
    private LocalDateTime expiryDate;
    private Integer daysExpired;
}
