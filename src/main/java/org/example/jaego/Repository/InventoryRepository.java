package org.example.jaego.Repository;

import org.example.jaego.Entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // === 기본 조회 ===
    Optional<Inventory> findByName(String name);
    boolean existsByName(String name);
    List<Inventory> findByCategoryIsNull(); // 미분류 재고

    // === 검색 기능 ===
    @Query("SELECT i FROM Inventory i WHERE i.name LIKE %:keyword% " +
            "ORDER BY i.name ASC")
    List<Inventory> searchByName(@Param("keyword") String keyword);

    @Query("SELECT i FROM Inventory i " +
            "WHERE i.name LIKE %:keyword% OR i.category.category LIKE %:keyword% " +
            "ORDER BY i.name ASC")
    List<Inventory> searchByNameOrCategory(@Param("keyword") String keyword);

    // === 재고 현황 조회 ===

    // 저재고 상품 (총수량 기준)
    @Query("SELECT i FROM Inventory i WHERE i.totalQuantity < :minQuantity " +
            "ORDER BY i.totalQuantity ASC")
    List<Inventory> findLowStockInventories(@Param("minQuantity") Integer minQuantity);

    // 임박 재고가 있는 상품들
    @Query("SELECT DISTINCT i FROM Inventory i " +
            "JOIN i.stockBatches sb " +
            "WHERE sb.expiryDate BETWEEN CURRENT_DATE AND :targetDate " +
            "AND sb.quantity > 0 " +
            "ORDER BY i.name ASC")
    List<Inventory> findInventoriesWithExpiringStock(@Param("targetDate") LocalDate targetDate);

    // 만료된 재고가 있는 상품들
    @Query("SELECT DISTINCT i FROM Inventory i " +
            "JOIN i.stockBatches sb " +
            "WHERE sb.expiryDate < CURRENT_DATE " +
            "AND sb.quantity > 0")
    List<Inventory> findInventoriesWithExpiredStock();


    // === 카테고리별 조회 ===
    List<Inventory> findByCategory_Category(String categoryName);

    @Query("SELECT i FROM Inventory i " +
            "WHERE i.category.category = :categoryName " +
            "ORDER BY i.totalQuantity ASC")
    List<Inventory> findByCategoryOrderByQuantity(@Param("categoryName") String categoryName);

    // === 통계 ===
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.category.category = :categoryName")
    Long countByCategory(@Param("categoryName") String categoryName);

    @Query("SELECT COALESCE(SUM(i.totalQuantity), 0) FROM Inventory i " +
            "WHERE i.category.category = :categoryName")
    Long sumQuantityByCategory(@Param("categoryName") String categoryName);

    // === 엑셀 업로드 관련 ===

    // 다중 상품명으로 일괄 조회 (엑셀 업로드용)
    @Query("SELECT i FROM Inventory i WHERE i.name IN :productNames ORDER BY i.name")
    List<Inventory> findByNameInOrderByName(@Param("productNames") List<String> productNames);
    // 상품명과 현재 재고량 함께 조회 (재고 확인용)
    @Query("SELECT i.name, i.totalQuantity FROM Inventory i WHERE i.name IN :productNames")
    List<Object[]> findNameAndQuantityByNames(@Param("productNames") List<String> productNames);
    // 재고 부족 상품 조회 (엑셀 처리 전 검증용)
    @Query("SELECT i FROM Inventory i WHERE i.name = :productName AND i.totalQuantity < :requiredQuantity")
    Optional<Inventory> findInsufficientStock(@Param("productName") String productName,
                                              @Param("requiredQuantity") Integer requiredQuantity);
    // 여러 상품의 재고 부족 상품들 일괄 조회
    @Query("SELECT i.name, i.totalQuantity FROM Inventory i WHERE i.name IN :productNames " +
            "AND i.totalQuantity < :minQuantity")
    List<Object[]> findInsufficientStockProducts(@Param("productNames") List<String> productNames,
                                                 @Param("minQuantity") Integer minQuantity);
    // 엑셀 처리 결과 로그용 - 처리된 상품 수량 업데이트 이력
    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.totalQuantity = :newQuantity, i.updatedAt = :updateTime " +
            "WHERE i.inventoryId = :inventoryId")
    void updateQuantityWithTimestamp(@Param("inventoryId") Long inventoryId,
                                     @Param("newQuantity") Integer newQuantity,
                                     @Param("updateTime") LocalDateTime updateTime);
    // 카테고리와 함께 상품 조회 (발주용)
    @Query("SELECT i FROM Inventory i LEFT JOIN i.category c WHERE i.name = :productName")
    Optional<Inventory> findByNameWithCategory(@Param("productName") String productName);
    // 미분류 상품들 (카테고리 배정이 필요한 상품들)
    @Query("SELECT i FROM Inventory i WHERE i.category IS NULL AND i.name IN :productNames")
    List<Inventory> findUncategorizedByNames(@Param("productNames") List<String> productNames);
    // 코드로 조회 (엑셀 업로드 시 사용)
    @Query("SELECT i FROM Inventory i WHERE i.name IN :names")
    List<Inventory> findByNameIn(@Param("names") List<String> names);

    // 총 수량 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE Inventory i SET i.totalQuantity = :totalQuantity " +
            "WHERE i.inventoryId = :inventoryId")
    void updateTotalQuantity(@Param("inventoryId") Long inventoryId,
                             @Param("totalQuantity") Integer totalQuantity);
}