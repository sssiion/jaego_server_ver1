package org.example.jaego.Exception;

public class BatchAlreadyExpiredException extends RuntimeException {
    public BatchAlreadyExpiredException(String message) {
        super(message);
    }
}