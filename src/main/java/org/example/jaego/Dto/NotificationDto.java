package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {
    private Long notificationId;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}
