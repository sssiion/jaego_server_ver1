package org.example.jaego.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventorySummaryDto {
    private Long inventoryId;
    private String name;
    private String categoryName;
    private Integer totalQuantity;
    private java.time.LocalDate earliestExpiryDate;
    private Long urgentBatchCount;
}
