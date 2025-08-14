package org.example.jaego.Dto;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ExpirationStatsDto {
    private Integer targetDays;
    private Long totalUrgentBatches;
    private Integer totalUrgentQuantity;
    private Long totalExpiredBatches;
    private Integer totalExpiredQuantity;
    private Map<String, Integer> categoryUrgentStats;
}
