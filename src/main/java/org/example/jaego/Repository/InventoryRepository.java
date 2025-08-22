package org.example.jaego.Repository;

import org.example.jaego.Dto.StockBatchDto;
import org.example.jaego.Entity.Category;
import org.example.jaego.Entity.Inventory;
import org.example.jaego.Entity.stockBatches;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {



    // 카테고리별 상품 조회 (빠른 기한 순)
    @Query("SELECT i FROM Inventory i " +
            "LEFT JOIN i.stockBatches sb " +
            "WHERE (:categoryId IS NULL AND i.category IS NULL) OR i.category.categoryId = :categoryId " +
            "GROUP BY i " +
            "ORDER BY MIN(COALESCE(sb.expiryDate, '9999-12-31'))")
    List<Inventory> findByCategoryIdOrderByEarliestExpiryDate(@Param("categoryId") Long categoryId);

    Inventory findInventoriesByStockBatchesContains(stockBatches batch);
    // 카테고리별 상품 검색
    List<Inventory> findByCategoryCategoryIdAndNameContainingIgnoreCaseOrderByName(Long categoryId, String name);

    // 전체 상품 검색
    @Query("SELECT i FROM Inventory i " +
            "LEFT JOIN i.stockBatches sb " +
            "WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "GROUP BY i " +
            "ORDER BY MIN(COALESCE(sb.expiryDate, '9999-12-31'))")
    List<Inventory> findByNameContainingIgnoreCaseOrderByEarliestExpiryDate(@Param("keyword") String keyword);


    // InventoryRepository.java
    List<Inventory> findByCategory_CategoryId(Long categoryId);
    // 상품명으로 정확 검색
    Optional<Inventory> findByName(String name);

    // 바코드로 상품 검색 (추후 바코드 필드 추가시 사용)
    // Optional<Inventory> findByBarcode(String barcode);

    // 카테고리별 상품 개수
    Long countByCategoryCategoryId(Long categoryId);


    // 임박 배치가 있는 상품 조회
    @Query("""
            SELECT DISTINCT i
            FROM Inventory i
            JOIN i.stockBatches sb
            WHERE sb.expiryDate IS NOT NULL
              AND sb.expiryDate <= :targetDate
              AND sb.quantity > 0
            ORDER BY i.name
        """)
    List<Inventory> findInventoriesWithUrgentBatches(@Param("days") int days);

    // 최근 업데이트된 상품 10개
    List<Inventory> findTop10ByOrderByUpdatedAtDesc();



    // 전체 상품 개수
    @Query("SELECT COUNT(i) FROM Inventory i")
    Long getTotalInventoryCount();

    // 만료된 배치가 있는 상품 조회
    @Query("SELECT DISTINCT i FROM Inventory i " +
            "INNER JOIN i.stockBatches sb " +
            "WHERE sb.expiryDate IS NOT NULL " +
            "AND sb.expiryDate < CURRENT_DATE " +
            "AND sb.quantity > 0 " +
            "ORDER BY i.name")
    List<Inventory> findInventoriesWithExpiredBatches();


    //엑셀
    @Query("SELECT i FROM Inventory i WHERE i.name IN :productNames ORDER BY i.name")
    List<Inventory> findByNameInOrderByName(@Param("productNames") List<String> productNames);

    @Query("SELECT i.name, i.totalQuantity FROM Inventory i WHERE i.name IN :productNames")
    List<Object[]> findNameAndQuantityByNames(@Param("productNames") List<String> productNames);

    @Query("SELECT i FROM Inventory i WHERE i.name = :productName AND i.totalQuantity < :requiredQuantity")
    Optional<Inventory> findInsufficientStock(@Param("productName") String productName,
                                              @Param("requiredQuantity") Integer requiredQuantity);

    @Query("SELECT i FROM Inventory i LEFT JOIN i.category c WHERE i.name = :productName")
    Optional<Inventory> findByNameWithCategory(@Param("productName") String productName);

    @Modifying
    @Query("UPDATE Inventory i SET i.totalQuantity = :totalQuantity " +
            "WHERE i.inventoryId = :inventoryId")
    void updateTotalQuantity(@Param("inventoryId") Long inventoryId, @Param("totalQuantity") Integer totalQuantity);


    @Query("SELECT i " +
            "FROM Inventory i " +
            "LEFT JOIN i.stockBatches b " + // stockBatches가 없는 Inventory도 포함하기 위해 LEFT JOIN 사용
            "GROUP BY i.inventoryId, i.totalQuantity " + // 각 Inventory 별로 그룹화
            // HAVING 절로 그룹화된 결과에 조건을 적용
            // COALESCE(SUM(b.quantity), 0) : b.quantity 합계가 NULL이면 0으로 처리
            "HAVING COALESCE(SUM(b.quantity), 0) < i.totalQuantity")
    List<Inventory> findInventoriesWithQuantityMismatch();
}