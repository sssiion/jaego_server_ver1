package org.example.jaego.Repository;

import org.example.jaego.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // === 기본 조회 ===
    Optional<Category> findByCategory(String category);
    boolean existsByCategory(String category);
    List<Category> findByCategoryType(String categoryType);

    // === 통계 ===
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.category.categoryId = :categoryId")
    Long countInventoriesByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT c.category, COUNT(i) as inventoryCount FROM Category c " +
            "LEFT JOIN c.inventories i GROUP BY c.categoryId, c.category")
    List<Object[]> getCategoryStatistics();
}