package org.example.jaego.Repository;

import org.example.jaego.Entity.stockBatches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockBatchRepository extends JpaRepository<stockBatches, Long> {
    // 상품별 배치 조회 (NULL 기한 우선)
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 0 ELSE 1 END, sb.expiryDate ASC")
    List<stockBatches> findByInventoryIdOrderByExpiryDateNullsFirst(@Param("inventoryId") Long inventoryId);
    //유통기한 수정
    @Modifying
    @Query("UPDATE stockBatches sb SET sb.expiryDate = :newExpiryDate WHERE sb.id = :batchId")
    int updateBatchExpiryDate(@Param("batchId") Long batchId,
                              @Param("newExpiryDate") LocalDateTime newExpiryDate);

    // 지정 일수 내 임박 배치 조회
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate <= :targetDate " +
            "AND sb.expiryDate >= CURRENT_TIMESTAMP " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.expiryDate ASC")
    List<stockBatches> findUrgentBatchesByDays(@Param("targetDate") LocalDateTime targetDate);

    // 만료된 배치 조회
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate < CURRENT_TIMESTAMP " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.expiryDate DESC")
    List<stockBatches> findExpiredBatches();

    // 특정 기간 내 만료 예정 배치
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.expiryDate BETWEEN :startDate AND :endDate " +
            "AND sb.quantity > 0 " +
            "ORDER BY sb.expiryDate ASC")
    List<stockBatches> findBatchesExpiringBetween(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // 상품별 총 수량 조회
    @Query("SELECT COALESCE(SUM(sb.quantity), 0) FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId")
    Integer getTotalQuantityByInventoryId(@Param("inventoryId") Long inventoryId);

    // 상품별 가장 빠른 기한 조회
    @Query("SELECT MIN(sb.expiryDate) FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.expiryDate IS NOT NULL " +
            "AND sb.quantity > 0")
    LocalDateTime getEarliestExpiryDateByInventoryId(@Param("inventoryId") Long inventoryId);

    // 임박 배치 개수 조회
    @Query("SELECT COUNT(sb) FROM stockBatches sb " +
            "WHERE sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate <= :targetDate " +
            "AND sb.expiryDate >= CURRENT_TIMESTAMP " +
            "AND sb.quantity > 0")
    Long countUrgentBatchesByDays(@Param("targetDate") LocalDateTime targetDate);

    // 만료된 배치 개수 조회
    @Query("SELECT COUNT(sb) FROM stockBatches sb " +
            "WHERE sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate < CURRENT_TIMESTAMP " +
            "AND sb.quantity > 0")
    Long countExpiredBatches();

    // 카테고리별 총 수량
    @Query("SELECT COALESCE(SUM(sb.quantity), 0) FROM stockBatches sb " +
            "WHERE sb.inventory.category.categoryId = :categoryId")
    Integer getTotalQuantityByCategory(@Param("categoryId") Long categoryId);

    // 카테고리별 임박 배치 개수
    @Query("SELECT COUNT(sb) FROM stockBatches sb " +
            "WHERE sb.inventory.category.categoryId = :categoryId " +
            "AND sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate <= :targetDate " +
            "AND sb.expiryDate >= CURRENT_TIMESTAMP " +
            "AND sb.quantity > 0")
    Long countUrgentBatchesByCategory(@Param("categoryId") Long categoryId,
                                      @Param("targetDate") LocalDateTime targetDate);

    // 상품별 가장 오래된 배치 조회 (FIFO)
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.quantity > 0 " +
            "ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 1 ELSE 0 END, sb.expiryDate ASC, sb.createdAt ASC")
    List<stockBatches> findOldestBatchesByInventoryId(@Param("inventoryId") Long inventoryId);

    // 배치 수량 차감
    @Modifying
    @Query("UPDATE stockBatches sb SET sb.quantity = sb.quantity - :reduceAmount " +
            "WHERE sb.id = :batchId AND sb.quantity >= :reduceAmount")
    int reduceQuantity(@Param("batchId") Long batchId, @Param("reduceAmount") Integer reduceAmount);

    // 빈 배치 삭제
    @Modifying
    @Query("DELETE FROM stockBatches sb WHERE sb.quantity <= 0")
    int deleteEmptyBatches();

    // 상품별 기간 내 배치 조회
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY sb.expiryDate ASC")
    List<stockBatches> findBatchesByInventoryAndDateRange(@Param("inventoryId") Long inventoryId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    // 배치 수량 업데이트 +날짜
    @Modifying
    @Query("UPDATE stockBatches sb SET sb.quantity = :newQuantity, sb.expiryDate = :newExpiryDate WHERE sb.id = :batchId")
    int updateBatch(@Param("batchId") Long batchId, @Param("newExpiryDate") LocalDateTime newExpiryDate,@Param("newQuantity") Integer newQuantity);

    // 엑셀
    // FIFO 처리를 위한 재고 배치 조회 (NULL 기한 우선)
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId AND sb.quantity > 0 " +
            "ORDER BY CASE WHEN sb.expiryDate IS NULL THEN 0 ELSE 1 END, " +
            "sb.expiryDate ASC")
    List<stockBatches> findForFIFOProcessing(@Param("inventoryId") Long inventoryId  );

    // 상품별 총 수량 계산
    @Query("SELECT COALESCE(SUM(sb.quantity), 0) FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId")
    Integer calculateTotalQuantityByInventoryId(@Param("inventoryId") Long inventoryId);

    // 유통기한 없는(Null) 배치 조회
    @Query("SELECT sb FROM stockBatches sb " +
            "WHERE sb.inventory.inventoryId = :inventoryId " +
            "AND sb.expiryDate IS NULL AND sb.quantity > 0")
    List<stockBatches> findNullExpiryBatchesByInventoryId(@Param("inventoryId") Long inventoryId);

    // 수량 0 이하 배치 삭제
    @Modifying
    @Query("DELETE FROM stockBatches sb " +
            "WHERE sb.quantity <= 0 AND sb.inventory.inventoryId = :inventoryId")
    void deleteEmptyBatchesByInventoryId(@Param("inventoryId") Long inventoryId);


    List<stockBatches> findExpiryBatchesByInventoryInventoryId(Long inventoryId);
}
