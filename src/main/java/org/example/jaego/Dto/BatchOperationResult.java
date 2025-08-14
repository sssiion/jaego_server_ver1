package org.example.jaego.Dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchOperationResult {
    private int totalProcessed;
    private int totalQuantity;
    private List<String> processedItems;
}
