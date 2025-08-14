package org.example.jaego.Dto;



import lombok.Data;

@Data
public class PushNotificationRequest {
    private Long userId;
    private String title;
    private String message;
}
