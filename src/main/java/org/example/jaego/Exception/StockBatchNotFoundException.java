package org.example.jaego.Exception;

public class StockBatchNotFoundException extends RuntimeException {
    public StockBatchNotFoundException(String message) {
        super(message);
    }
}