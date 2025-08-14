package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UrgentInventoryDto {
    private Long inventoryId;
    private String name;
    private String categoryName;
    private Integer totalQuantity;
    private LocalDate earliestExpiryDate;
    private Integer daysRemaining;

}

