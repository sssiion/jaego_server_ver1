package org.example.jaego.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private Long settingsId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // 사용자 ID (추후 User 엔티티와 연관관계 설정 가능)

    // 알림 설정
    @Column(name = "alert_threshold", nullable = false)
    @Builder.Default
    private Integer alertThreshold = 7; // 기본 7일 전 알림

    @Column(name = "enable_expiry_alerts", nullable = false)
    @Builder.Default
    private Boolean enableExpiryAlerts = true;

    @Column(name = "enable_low_stock_alerts", nullable = false)
    @Builder.Default
    private Boolean enableLowStockAlerts = true;

    @Column(name = "alert_frequency", length = 20)
    @Builder.Default
    private String alertFrequency = "DAILY"; // DAILY, WEEKLY, REAL_TIME

    // 재고 관리 설정
    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 5; // 부족 재고 기준

    @Column(name = "auto_delete_expired", nullable = false)
    @Builder.Default
    private Boolean autoDeleteExpired = false;

    // 디스플레이 설정
    @Column(name = "default_sort_order", length = 20)
    @Builder.Default
    private String defaultSortOrder = "EXPIRY_DATE"; // EXPIRY_DATE, NAME, QUANTITY

    @Column(name = "items_per_page")
    @Builder.Default
    private Integer itemsPerPage = 20;

    @Column(name = "theme", length = 10)
    @Builder.Default
    private String theme = "LIGHT"; // LIGHT, DARK

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 비즈니스 메서드
    public boolean isExpiryAlertEnabled() {
        return this.enableExpiryAlerts != null && this.enableExpiryAlerts;
    }

    public boolean isLowStockAlertEnabled() {
        return this.enableLowStockAlerts != null && this.enableLowStockAlerts;
    }

    public boolean isDailyAlert() {
        return "DAILY".equals(this.alertFrequency);
    }

    public boolean isWeeklyAlert() {
        return "WEEKLY".equals(this.alertFrequency);
    }

    public boolean isRealTimeAlert() {
        return "REAL_TIME".equals(this.alertFrequency);
    }
}