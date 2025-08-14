package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExpiredBatchDto {
    private Long id;
    private Long inventoryId;
    private String inventoryName;
    private String categoryName;
    private Integer quantity;
    private LocalDate expiryDate;
    private Integer daysExpired;
}
