package org.example.jaego.Exception;

public class NotificationSendFailedException extends RuntimeException {
    public NotificationSendFailedException(String message) {
        super(message);
    }
}