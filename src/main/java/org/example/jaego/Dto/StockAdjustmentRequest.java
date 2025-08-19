package org.example.jaego.Dto;



import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockAdjustmentRequest {
    private Long inventoryId;
    private Integer quantity;
    private LocalDateTime expiryDate; // 재고 추가 시 선택
}
