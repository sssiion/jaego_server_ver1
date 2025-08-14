package org.example.jaego.Exception;

public class StockOperationFailedException extends RuntimeException {
    public StockOperationFailedException(String message) {
        super(message);
    }
}