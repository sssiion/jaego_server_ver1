package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockMovementDto {
    private Long movementId;
    private Long inventoryId;
    private String movementType; // IN, OUT, ADJUST, EXPIRE
    private Integer quantity;
    private String reason;
    private Long createdBy;
    private LocalDateTime createdAt;
}
