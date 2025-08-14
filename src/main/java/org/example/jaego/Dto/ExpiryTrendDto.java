package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExpiryTrendDto {
    private LocalDate date;
    private Long expiringBatchCount;
    private Integer expiringQuantity;
}
