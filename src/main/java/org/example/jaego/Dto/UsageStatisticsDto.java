package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UsageStatisticsDto {
    private Long inventoryId;
    private String inventoryName;
    private String movementType;
    private Integer quantity;
    private LocalDate date; // 사용 날짜
}
