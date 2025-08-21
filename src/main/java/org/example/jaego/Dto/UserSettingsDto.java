package org.example.jaego.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSettingsDto {
    private Long userId;
    private Integer alertThreshold;
    private Boolean enableExpiryAlerts;
    private Integer lowStockThreshold;
    private Boolean enableLowStockAlerts;
    private String theme;
    // ... 필요한 다른 설정 필드들
}
