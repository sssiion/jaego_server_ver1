package org.example.jaego.Repository;

import org.example.jaego.Entity.Stock_Batches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockBatchRepository extends JpaRepository<Stock_Batches, Long> {
    // === 기본 조회 기능 ===
    List<Stock_Batches> findByInventory_InventoryId(Long inventoryId);

    // FIFO용: 유통기한 순 정렬 (null은 마지막)
    @Query("SELECT sb FROM Stock_Batches sb WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.quantity > 0 " +
            "ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 1 ELSE 0 END, sb.expiryDate ASC")
    List<Stock_Batches> findByInventoryIdForFIFO(@Param("inventoryId") Long inventoryId);

    // === 유통기한 관련 조회 ===

    // 유통기한 임박 재고 (N일 이내)
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate BETWEEN CURRENT_DATE AND :targetDate " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.expiryDate ASC")
    List<Stock_Batches> findExpiringWithin(@Param("targetDate") LocalDate targetDate);

    // 유통기한 임박 재고 (분 단위)
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate <= :targetDateTime " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.expiryDate ASC")
    List<Stock_Batches> findExpiringWithinMinutes(@Param("targetDateTime") LocalDate targetDateTime);

    // 특정 기간 내 유통기한 재고
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.expiryDate BETWEEN :startDate AND :endDate " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.expiryDate ASC")
    List<Stock_Batches> findByExpiryDateBetween(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // === 검색 기능 ===

    // 재고명으로 검색 (유통기한 순 정렬)
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.inventory.name LIKE %:keyword% " +
            "AND sb.quantity > 0 " +
            "ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 1 ELSE 0 END, sb.expiryDate ASC")
    List<Stock_Batches> searchByInventoryName(@Param("keyword") String keyword);

    // 카테고리로 검색 (유통기한 순 정렬)
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.inventory.category.category LIKE %:categoryName% " +
            "AND sb.quantity > 0 " +
            "ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 1 ELSE 0 END, sb.expiryDate ASC")
    List<Stock_Batches> searchByCategory(@Param("categoryName") String categoryName);

    // === 통계 및 집계 ===

    // 재고별 총 수량 계산
    @Query("SELECT COALESCE(SUM(sb.quantity), 0) FROM Stock_Batches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId")
    Integer calculateTotalQuantityByInventoryId(@Param("inventoryId") Long inventoryId);

    // 카테고리별 임박 재고 개수
    @Query("SELECT COUNT(sb) FROM Stock_Batches sb " +
            "WHERE sb.inventory.category.category = :categoryName " +
            "AND sb.expiryDate BETWEEN CURRENT_DATE AND :targetDate " +
            "AND sb.quantity > 0")
    Long countExpiringByCategory(@Param("categoryName") String categoryName,
                                 @Param("targetDate") LocalDate targetDate);
    // === 데이터 수정 ===

    // FIFO 기반 수량 차감
    @Modifying
    @Transactional
    @Query("UPDATE Stock_Batches sb SET sb.quantity = :newQuantity " +
            "WHERE sb.id = :batchId")
    void updateQuantity(@Param("batchId") Long batchId, @Param("newQuantity") Integer newQuantity);

    // 수량이 0인 배치 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Stock_Batches sb WHERE sb.quantity = 0")
    void deleteEmptyBatches();

    // 특정 재고의 빈 배치 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Stock_Batches sb WHERE sb.inventory.inventoryId = :inventoryId AND sb.quantity = 0")
    void deleteEmptyBatchesByInventoryId(@Param("inventoryId") Long inventoryId);
    // === NULL 유통기한 전용 조회 ===

    // 특정 재고의 null 유통기한 배치들만 조회
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.expiryDate IS NULL " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.createdAt ASC")
    List<Stock_Batches> findNullExpiryBatchesByInventoryId(@Param("inventoryId") Long inventoryId);

    // 모든 null 유통기한 배치 조회 (전체 시스템)
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.expiryDate IS NULL " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.inventory.name ASC, sb.createdAt ASC")
    List<Stock_Batches> findAllNullExpiryBatches();

    // 카테고리별 null 유통기한 배치 조회
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.inventory.category.category = :categoryName " +
            "AND sb.expiryDate IS NULL " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.inventory.name ASC")
    List<Stock_Batches> findNullExpiryBatchesByCategory(@Param("categoryName") String categoryName);

    // null 유통기한 재고 검색
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.inventory.name LIKE %:keyword% " +
            "AND sb.expiryDate IS NULL " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.inventory.name ASC")
    List<Stock_Batches> searchNullExpiryBatches(@Param("keyword") String keyword);

    // === NULL vs 유통기한 있는 재고 분리 조회 ===

    // 유통기한 있는 배치들만 조회 (FIFO용)
    @Query("SELECT sb FROM Stock_Batches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.expiryDate IS NOT NULL " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.expiryDate ASC")
    List<Stock_Batches> findExpiryBatchesByInventoryId(@Param("inventoryId") Long inventoryId);

    // === 통계 (NULL 유통기한) ===

    // null 유통기한 총 수량
    @Query("SELECT COALESCE(SUM(sb.quantity), 0) FROM Stock_Batches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.expiryDate IS NULL")
    Integer calculateNullExpiryQuantity(@Param("inventoryId") Long inventoryId);

    // 카테고리별 null 유통기한 재고 개수
    @Query("SELECT COUNT(sb) FROM Stock_Batches sb " +
            "WHERE sb.inventory.category.category = :categoryName " +
            "AND sb.expiryDate IS NULL " +
            "AND sb.quantity > 0")
    Long countNullExpiryByCategory(@Param("categoryName") String categoryName);
    // === 엑셀 처리 전용 FIFO 메서드들 ===

    // FIFO용 배치 조회 (null 우선, 그 다음 유통기한 순)
    @Query("SELECT sb FROM Stock_Batches sb WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.quantity > 0 " +
            "ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 0 ELSE 1 END, " +
            "COALESCE(sb.expiryDate, '9999-12-31') ASC, sb.createdAt ASC")
    List<Stock_Batches> findForFIFOProcessing(@Param("inventoryId") Long inventoryId);

    // 여러 상품의 FIFO 배치들 일괄 조회
    @Query("SELECT sb FROM Stock_Batches sb WHERE sb.inventory.inventoryId IN :inventoryIds " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.inventory.inventoryId, " +
            "CASE WHEN sb.expiryDate IS NULL THEN 0 ELSE 1 END, " +
            "COALESCE(sb.expiryDate, '9999-12-31') ASC")
    List<Stock_Batches> findBatchesForMultipleInventories(@Param("inventoryIds") List<Long> inventoryIds);

    // 엑셀 처리용 배치 대량 수정
    @Modifying
    @Transactional
    @Query("UPDATE Stock_Batches sb SET sb.quantity = :newQuantity WHERE sb.id = :batchId")
    int updateBatchQuantity(@Param("batchId") Long batchId, @Param("newQuantity") Integer newQuantity);

    // 엑셀 처리용 빈 배치 대량 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM Stock_Batches sb WHERE sb.quantity <= 0 AND sb.inventory.inventoryId IN :inventoryIds")
    int deleteEmptyBatchesByInventoryIds(@Param("inventoryIds") List<Long> inventoryIds);

    // 상품별 현재 배치 현황 조회 (엑셀 처리 결과 확인용)
    @Query("SELECT sb.inventory.name, COUNT(sb), SUM(sb.quantity) FROM Stock_Batches sb " +
            "WHERE sb.inventory.inventoryId IN :inventoryIds AND sb.quantity > 0 " +
            "GROUP BY sb.inventory.inventoryId, sb.inventory.name")
    List<Object[]> getBatchSummaryByInventoryIds(@Param("inventoryIds") List<Long> inventoryIds);


}
