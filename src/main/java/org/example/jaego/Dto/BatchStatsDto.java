package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BatchStatsDto {
    private Long inventoryId;
    private Integer totalQuantity;
    private LocalDateTime earliestExpiryDate;
    private Long urgentBatchCount;
    private Long expiredBatchCount;
}
