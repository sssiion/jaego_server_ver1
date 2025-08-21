package org.example.jaego.Service;

import org.example.jaego.Dto.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StockBatchService {


    // 재고별 배치 조회
    List<StockBatchDto> getBatchesByInventoryId(Long inventoryId);

    // 임박 배치 조회 (일수 지정)
    List<UrgentBatchDto> getUrgentBatches(Integer days);

    // 만료된 배치 조회
    List<ExpiredBatchDto> getExpiredBatches();

    // 새 배치 생성
    StockBatchDto createBatch(StockBatchCreateRequest request);

    // null 배치 가져오기
    List<StockBatchDto> getnullBatches();

    // 배치 삭제
    void deleteBatch(Long batchId);

    // 재고 차감 (FIFO 방식)
    OperationResult reduceStock(StockReductionRequest request);

    // 재고 추가
    StockBatchDto addStock(StockAdditionRequest request);

    // 만료 배치 처리
    BatchOperationResult processBatchExpiration();

    // 재고별 배치 통계
    BatchStatsDto getBatchStatsByInventory(Long inventoryId);

    // 배치 데이터 유효성 검사
    boolean validateBatchData(StockBatchCreateRequest request);

    // 배치 상세 조회
    StockBatchDto getBatchById(Long batchId);


    //배치 기한+수량 수정
    void updateBatch(Long batchId, LocalDateTime newExpiryDate, Integer newQuantity);

    // 배치 오류 사항 수정
    void SettingBatch();
}
