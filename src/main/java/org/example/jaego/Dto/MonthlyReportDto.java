package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MonthlyReportDto {
    private int year;
    private int month;
    private Long totalExpiredBatches;
    private Integer totalExpiredQuantity;
    private Map<String, Integer> categoryExpiredStats;
    private List<ExpiryTrendDto> dailyExpiryTrend;
}
