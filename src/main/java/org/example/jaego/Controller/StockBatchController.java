package org.example.jaego.Controller;


import lombok.RequiredArgsConstructor;

import org.example.jaego.Service.StockBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.jaego.Dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class StockBatchController {

    private final StockBatchService stockBatchService;

    @GetMapping("/inventory/{inventoryId}")
    public List<StockBatchDto> getBatchesByInventory(@PathVariable Long inventoryId) {
        return stockBatchService.getBatchesByInventoryId(inventoryId);
    }

    @PostMapping
    public ResponseEntity<StockBatchDto> createBatch(@RequestBody StockBatchCreateRequest request) {
        return ResponseEntity.ok(stockBatchService.createBatch(request));
    }

    @PutMapping("/{batchId}")
    public ResponseEntity<StockBatchDto> updateBatchQuantity(@PathVariable Long batchId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(stockBatchService.updateBatchQuantity(batchId, quantity));
    }

    @DeleteMapping("/{batchId}")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long batchId) {
        stockBatchService.deleteBatch(batchId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reduce")
    public OperationResult reduceStock(@RequestBody StockReductionRequest request) {
        return stockBatchService.reduceStock(request);
    }

    @PostMapping("/add")
    public StockBatchDto addStock(@RequestBody StockAdditionRequest request) {
        return stockBatchService.addStock(request);
    }
}
