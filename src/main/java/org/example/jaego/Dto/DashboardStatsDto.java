package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsDto {
    private Long totalInventories;
    private Integer totalQuantity;
    private Long totalCategories;
    private Long urgentProducts;
    private Long expiredProducts;
    private Double urgentPercentage;
}
