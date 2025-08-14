package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrgentProductsStatsDto {
    private Long totalUrgentBatches;
    private Integer totalUrgentQuantity;
    private java.util.Map<String, Long> categoryUrgentCount;
    private java.util.List<UrgentInventoryDto> topUrgentProducts;
    private Integer targetDays;
}
