package org.example.jaego.Controller;


import lombok.RequiredArgsConstructor;

import org.example.jaego.Service.StockBatchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.jaego.Dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class StockBatchController {

    private final StockBatchService stockBatchService;

    //배치 리스트 가져오기
    @GetMapping("/inventory/{inventoryId}")
    public List<StockBatchDto> getBatchesByInventory(@PathVariable Long inventoryId) {
        return stockBatchService.getBatchesByInventoryId(inventoryId);
    }
    @GetMapping("/urgent/{days}")
    public List<UrgentBatchDto> getBatchesUrgent(@PathVariable Integer days) {
        return stockBatchService.getUrgentBatches(days);
    }

    @PostMapping
    public ResponseEntity<StockBatchDto> createBatch(@RequestBody StockBatchCreateRequest request) {
        return ResponseEntity.ok(stockBatchService.createBatch(request));
    }
    //유통기한 업로드 +수량
    @PostMapping("/{batchId}")
    public void updateBatchExpire(@PathVariable Long batchId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime request, @RequestParam Integer quantity) {
         stockBatchService.updateBatch(batchId, request, quantity);
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
