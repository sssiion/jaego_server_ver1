package org.example.jaego.Controller;



import lombok.RequiredArgsConstructor;
import org.example.jaego.Dto.*;
import org.example.jaego.Service.StockAdjustmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adjustments")
@RequiredArgsConstructor
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;

    // 재고 추가
    @PostMapping("/increase")
    public ResponseEntity<OperationResult> increaseStock(@RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockAdjustmentService.increaseStock(request));
    }

    // 재고 차감
    @PostMapping("/decrease")
    public ResponseEntity<OperationResult> decreaseStock(@RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockAdjustmentService.decreaseStock(request));
    }

    // 재고 수량 직접 수정
    @PostMapping("/adjust")
    public ResponseEntity<OperationResult> adjustQuantity(@RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockAdjustmentService.adjustStockQuantity(request));
    }

    // 여러 상품 일괄 조정
    @PostMapping("/bulk")
    public ResponseEntity<OperationResult> bulkAdjustment(@RequestBody BulkAdjustmentRequest request) {
        return ResponseEntity.ok(stockAdjustmentService.bulkStockAdjustment(request));
    }
}
