package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryTurnoverDto {
    private Long inventoryId;
    private String inventoryName;
    private String categoryName;
    private Integer totalOutbound;
    private Integer averageInventory;
    private Double turnoverRate;
}
