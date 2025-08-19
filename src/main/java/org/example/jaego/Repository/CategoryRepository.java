package org.example.jaego.Repository;

import org.example.jaego.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 모든 카테고리 조회
    List<Category> findAllByOrderByCategory();

    // 카테고리 타입별 조회 (유통기한/소비기한)
    List<Category> findByCategoryTypeOrderByCategory(String categoryType);

    // 카테고리 이름으로  찿기
    @Query("SELECT c FROM Category c WHERE c.category = :categoryType ORDER BY c.category")
    List<Category> findByCategoryType(@Param("categoryType") String categoryType);

    // 유통기한 카테고리만 조회
    @Query("SELECT c FROM Category c WHERE c.categoryType = '유통기한' ORDER BY c.category")
    List<Category> findExpirationCategories();

    // 소비기한 카테고리만 조회
    @Query("SELECT c FROM Category c WHERE c.categoryType = '소비기한' ORDER BY c.category")
    List<Category> findConsumptionCategories();

    // 카테고리명으로 검색
    Optional<Category> findByCategory(String category);

    // 카테고리명 중복 체크
    boolean existsByCategory(String category);



    // 재고가 있는 활성 카테고리 조회
    @Query("SELECT DISTINCT c FROM Category c INNER JOIN c.inventories i WHERE SIZE(i.stockBatches) > 0 ORDER BY c.category")
    List<Category> findActiveCategoriesWithInventoryCount();

    // 카테고리별 상품 개수 조회
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.category.categoryId = :categoryId")
    Long countInventoriesByCategoryId(@Param("categoryId") Long categoryId);

    //엑셀

}