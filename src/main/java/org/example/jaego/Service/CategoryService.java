package org.example.jaego.Service;
import org.example.jaego.Dto.*;



import java.util.List;

public interface CategoryService {

    // 재고 개수 포함 전체 카테고리 조회
    List<CategoryWithInventoryCountDto> getAllCategoriesWithInventoryCount();

    // 타입별 카테고리 조회 (유통기한/소비기한)
    List<CategoryDto> getCategoriesByType(String categoryType);

    // 새 카테고리 생성
    CategoryDto createCategory(CategoryCreateRequest request);

    // 카테고리 수정
    CategoryDto updateCategory(Long categoryId, CategoryUpdateRequest request);

    // 카테고리 삭제
    void deleteCategory(Long categoryId);

    // 카테고리별 재고 개수 조회
    Long getInventoryCountByCategory(Long categoryId);

    // 카테고리별 통계 정보 조회
    CategoryStatsDto getCategoryStats(Long categoryId);

    // 카테고리명 유효성 검사
    boolean validateCategoryName(String categoryName);

    // 카테고리 상세 조회
    CategoryDto getCategoryById(Long categoryId);

    // 모든 카테고리 조회 (기본)
    List<CategoryDto> getAllCategories();
}