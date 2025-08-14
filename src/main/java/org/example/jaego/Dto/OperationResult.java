package org.example.jaego.Dto;



import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OperationResult {
    private boolean success;
    private String message;
    private List<String> details;

    public static OperationResult success(String message) {
        return OperationResult.builder().success(true).message(message).build();
    }

    public static OperationResult failure(String message) {
        return OperationResult.builder().success(false).message(message).build();
    }
}
