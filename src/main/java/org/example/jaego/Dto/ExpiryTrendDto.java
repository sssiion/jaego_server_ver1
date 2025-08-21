package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExpiryTrendDto {
    private LocalDateTime date;
    private Long expiringBatchCount;
    private Integer expiringQuantity;
}
