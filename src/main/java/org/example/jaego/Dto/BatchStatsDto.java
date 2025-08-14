package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BatchStatsDto {
    private Long inventoryId;
    private Integer totalQuantity;
    private LocalDate earliestExpiryDate;
    private Long urgentBatchCount;
    private Long expiredBatchCount;
}
