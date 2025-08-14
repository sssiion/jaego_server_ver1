package org.example.jaego.Controller;



import lombok.RequiredArgsConstructor;
import org.example.jaego.Dto.*;
import org.example.jaego.Service.StockMovementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    // 이동 이력 기록
    @PostMapping
    public ResponseEntity<OperationResult> recordStockUsage(@RequestBody StockMovementDto dto) {
        return ResponseEntity.ok(stockMovementService.recordStockUsage(dto));
    }

    // 특정 재고의 이동 이력 조회
    @GetMapping("/inventory/{inventoryId}")
    public List<StockMovementDto> getStockHistory(@PathVariable Long inventoryId) {
        return stockMovementService.getStockHistory(inventoryId);
    }

    // 기간별 사용 통계 조회
    @GetMapping("/statistics")
    public List<UsageStatisticsDto> getUsageStatistics(@RequestParam String start,
                                                       @RequestParam String end) {
        return stockMovementService.getUsageStatistics(
                LocalDateTime.parse(start),
                LocalDateTime.parse(end)
        );
    }
}
