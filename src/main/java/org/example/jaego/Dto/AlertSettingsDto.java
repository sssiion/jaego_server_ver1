package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlertSettingsDto {
    private Long userId;
    private Integer alertThreshold;
    private String alertFrequency; // DAILY, WEEKLY, REAL_TIME
    private Boolean enableExpiryAlerts;
    private Boolean enableLowStockAlerts;
}
