package org.example.jaego.Service;
import org.example.jaego.Dto.*;


public interface StockAdjustmentService {

    // 재고 추가
    OperationResult increaseStock(StockAdjustmentRequest request);

    // 재고 차감
    OperationResult decreaseStock(StockAdjustmentRequest request);

    // 수량 직접 수정
    OperationResult adjustStockQuantity(StockAdjustmentRequest request);

    // 여러 상품 일괄 조정
    OperationResult bulkStockAdjustment(BulkAdjustmentRequest request);
}
