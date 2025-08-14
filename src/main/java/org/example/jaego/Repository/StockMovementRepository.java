package org.example.jaego.Repository;




import org.example.jaego.Entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    // 상품별 이동 이력 조회 (수정됨: inventory.inventoryId 사용)
    List<StockMovement> findByInventoryInventoryIdOrderByCreatedAtDesc(Long inventoryId);

    // 최근 이동 내역 조회 (limit 매개변수 제거하고 Pageable 사용 권장)
    @Query("SELECT sm FROM StockMovement sm ORDER BY sm.createdAt DESC")
    List<StockMovement> findRecentMovements();

    // 최근 이동 내역 조회 (지정된 개수만큼)
    @Query(value = "SELECT sm FROM StockMovement sm ORDER BY sm.createdAt DESC",
            nativeQuery = false)
    List<StockMovement> findTop10ByOrderByCreatedAtDesc();

    // 상품별 총 사용량 (수정됨: inventory.inventoryId 사용)
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm " +
            "WHERE sm.inventory.inventoryId = :inventoryId AND sm.movementType = 'OUT'")
    Integer getTotalUsageByInventory(@Param("inventoryId") Long inventoryId);

    // 기간별 사용 통계
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> getUsageStatisticsByPeriod(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // 추가 메서드들

    // 특정 이동 타입별 조회
    List<StockMovement> findByMovementTypeOrderByCreatedAtDesc(String movementType);

    // 상품과 이동 타입별 조회
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.inventory.inventoryId = :inventoryId " +
            "AND sm.movementType = :movementType " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> findByInventoryIdAndMovementType(@Param("inventoryId") Long inventoryId,
                                                         @Param("movementType") String movementType);

    // 특정 배치의 이동 이력 조회
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.stockBatch.id = :batchId " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> findByBatchId(@Param("batchId") Long batchId);

    // 기간별 상품별 총 입고량
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm " +
            "WHERE sm.inventory.inventoryId = :inventoryId " +
            "AND sm.movementType = 'IN' " +
            "AND sm.createdAt BETWEEN :startDate AND :endDate")
    Integer getTotalInboundByInventoryAndPeriod(@Param("inventoryId") Long inventoryId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    // 기간별 상품별 총 출고량
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm " +
            "WHERE sm.inventory.inventoryId = :inventoryId " +
            "AND sm.movementType = 'OUT' " +
            "AND sm.createdAt BETWEEN :startDate AND :endDate")
    Integer getTotalOutboundByInventoryAndPeriod(@Param("inventoryId") Long inventoryId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    // 카테고리별 이동 이력 조회
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.inventory.category.categoryId = :categoryId " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> findByCategoryId(@Param("categoryId") Long categoryId);

    // 작업자별 이동 이력 조회
    List<StockMovement> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    // 사유별 이동 이력 조회
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.reason LIKE CONCAT('%', :reason, '%') " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> findByReasonContaining(@Param("reason") String reason);

    // 특정 기간 내 만료로 처리된 이동 조회
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.movementType = 'EXPIRE' " +
            "AND sm.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> findExpiredMovementsByPeriod(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    // 조정 이동만 조회
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.movementType = 'ADJUST' " +
            "ORDER BY sm.createdAt DESC")
    List<StockMovement> findAdjustmentMovements();

    // 상품별 최근 이동 이력 (최대 N개)
    @Query("SELECT sm FROM StockMovement sm " +
            "WHERE sm.inventory.inventoryId = :inventoryId " +
            "ORDER BY sm.createdAt DESC " +
            "LIMIT :limit")
    List<StockMovement> findRecentMovementsByInventory(@Param("inventoryId") Long inventoryId,
                                                       @Param("limit") Integer limit);

    // 이동량 통계 (입고/출고/조정별)
    @Query("SELECT sm.movementType, COALESCE(SUM(sm.quantity), 0) " +
            "FROM StockMovement sm " +
            "WHERE sm.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY sm.movementType")
    List<Object[]> getMovementStatsByPeriod(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // 상품별 이동량 통계
    @Query("SELECT sm.movementType, COALESCE(SUM(sm.quantity), 0) " +
            "FROM StockMovement sm " +
            "WHERE sm.inventory.inventoryId = :inventoryId " +
            "AND sm.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY sm.movementType")
    List<Object[]> getMovementStatsByInventoryAndPeriod(@Param("inventoryId") Long inventoryId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);
}
