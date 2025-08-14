package org.example.jaego.Dto;



import lombok.Data;

import java.util.List;

@Data
public class BulkAdjustmentRequest {
    private List<StockAdjustmentRequest> adjustments;
}
