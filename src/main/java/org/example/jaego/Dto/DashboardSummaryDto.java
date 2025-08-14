package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DashboardSummaryDto {
    private Long totalInventories;
    private Integer totalQuantity;
    private Long urgentProducts;
    private Long expiredProducts;
    private Long thisWeekExpiring;
    private Long thisMonthExpiring;
    private List<UrgentInventoryDto> topUrgentProducts;
    private LocalDateTime lastUpdated;
}
