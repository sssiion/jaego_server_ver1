package org.example.jaego.Dto;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StockBatchUpdateRequest {
    private Integer quantity;
    private LocalDateTime expiryDate;

}
