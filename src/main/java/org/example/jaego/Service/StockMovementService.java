package org.example.jaego.Service;

import org.example.jaego.Dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementService {

    // 사용 내역 기록
    OperationResult recordStockUsage(StockMovementDto movementDto);

    // 재고 사용 이력 조회
    List<StockMovementDto> getStockHistory(Long inventoryId);

    // 사용량 통계
    List<UsageStatisticsDto> getUsageStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
