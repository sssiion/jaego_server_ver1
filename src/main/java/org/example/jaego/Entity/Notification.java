package org.example.jaego.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // EXPIRY_ALERT, LOW_STOCK, SYSTEM, INFO

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "priority", length = 10)
    @Builder.Default
    private String priority = "NORMAL"; // HIGH, NORMAL, LOW

    // 관련 엔티티 정보
    @Column(name = "related_inventory_id")
    private Long relatedInventoryId;

    @Column(name = "related_category_id")
    private Long relatedCategoryId;

    @Column(name = "related_batch_id")
    private Long relatedBatchId;

    // 알림 상태
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "is_sent", nullable = false)
    @Builder.Default
    private Boolean isSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // 알림 채널 (추후 확장용)
    @Column(name = "channel", length = 20)
    @Builder.Default
    private String channel = "APP"; // APP, EMAIL, SMS, PUSH

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 만료일 (자동 삭제용)
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // 비즈니스 메서드
    public boolean isUnread() {
        return this.isRead == null || !this.isRead;
    }

    public boolean isHighPriority() {
        return "HIGH".equals(this.priority);
    }

    public boolean isExpiryAlert() {
        return "EXPIRY_ALERT".equals(this.type);
    }

    public boolean isLowStockAlert() {
        return "LOW_STOCK".equals(this.type);
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.isBefore(LocalDateTime.now());
    }
}