package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class StockReductionRequest {
    private Long inventoryId;
    private Integer quantity;
}
