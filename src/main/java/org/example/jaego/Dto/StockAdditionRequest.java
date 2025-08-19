package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class StockAdditionRequest {
    private Long inventoryId;
    private Integer quantity;
    private LocalDateTime expiryDate;
}
