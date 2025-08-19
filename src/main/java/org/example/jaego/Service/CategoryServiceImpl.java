package org.example.jaego.Service;

import org.example.jaego.Entity.Category;
import org.example.jaego.Dto.*;

import org.example.jaego.Exception.CategoryInUseException;
import org.example.jaego.Exception.CategoryNotFoundException;
import org.example.jaego.Exception.DuplicateCategoryException;
import org.example.jaego.Repository.CategoryRepository;
import org.example.jaego.Repository.StockBatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StockBatchRepository stockBatchesRepository;

    @Override
    public List<CategoryDto> findByCategoryName(String categoryName){
        List<Category> categoryList = categoryRepository.findByCategoryTypeOrderByCategory(categoryName);
        return categoryList.stream()
                            .map(this::convertToDto)
                            .collect(Collectors.toList());

    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryWithInventoryCountDto> getAllCategoriesWithInventoryCount() {
        List<Category> categories = categoryRepository.findAllByOrderByCategory();

        return categories.stream()
                .map(category -> {
                    Long inventoryCount = categoryRepository.countInventoriesByCategoryId(category.getCategoryId());
                    return CategoryWithInventoryCountDto.builder()
                            .categoryId(category.getCategoryId())
                            .category(category.getCategory())
                            .categoryType(category.getCategoryType())
                            .inventoryCount(inventoryCount)
                            .createdAt(category.getCreatedAt())
                            .updatedAt(category.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesByType(String categoryType) {
        List<Category> categories = categoryRepository.findByCategoryTypeOrderByCategory(categoryType);

        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto createCategory(CategoryCreateRequest request) {
        // 카테고리명 중복 검사
        if (categoryRepository.existsByCategory(request.getCategory())) {
            throw new DuplicateCategoryException("이미 존재하는 카테고리명입니다: " + request.getCategory());
        }

        // 유효성 검사
        if (!validateCategoryName(request.getCategory())) {
            throw new IllegalArgumentException("유효하지 않은 카테고리명입니다.");
        }

        Category category = Category.builder()
                .category(request.getCategory())
                .categoryType(request.getCategoryType())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return convertToDto(savedCategory);
    }
    @Override
    public void updateCategories(List<Long> dto, String type){
        for (Long id : dto) {
            Category category = categoryRepository.findById(id)
                            .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + id));
            category.setCategoryType(type);
            categoryRepository.save(category);
        }
    }


    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

        // 카테고리명이 변경되는 경우 중복 검사
        if (!category.getCategory().equals(request.getCategory()) &&
                categoryRepository.existsByCategory(request.getCategory())) {
            throw new DuplicateCategoryException("이미 존재하는 카테고리명입니다: " + request.getCategory());
        }

        category.setCategory(request.getCategory());
        category.setCategoryType(request.getCategoryType());

        Category updatedCategory = categoryRepository.save(category);
        return convertToDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

        // 사용 중인 카테고리인지 확인
        Long inventoryCount = categoryRepository.countInventoriesByCategoryId(categoryId);
        if (inventoryCount > 0) {
            throw new CategoryInUseException("사용 중인 카테고리는 삭제할 수 없습니다. 재고 개수: " + inventoryCount);
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getInventoryCountByCategory(Long categoryId) {
        return categoryRepository.countInventoriesByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryStatsDto getCategoryStats(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

        Long inventoryCount = categoryRepository.countInventoriesByCategoryId(categoryId);
        Integer totalQuantity = stockBatchesRepository.getTotalQuantityByCategory(categoryId);
        Long urgentBatchCount = stockBatchesRepository.countUrgentBatchesByCategory(categoryId,
                java.time.LocalDateTime.now().plusDays(7));

        return CategoryStatsDto.builder()
                .categoryId(categoryId)
                .categoryName(category.getCategory())
                .categoryType(category.getCategoryType())
                .inventoryCount(inventoryCount)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                .urgentBatchCount(urgentBatchCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return false;
        }

        String trimmedName = categoryName.trim();
        return trimmedName.length() >= 2 && trimmedName.length() <= 50;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

        return convertToDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderByCategory();

        return categories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private CategoryDto convertToDto(Category category) {
        return CategoryDto.builder()
                .categoryId(category.getCategoryId())
                .category(category.getCategory())
                .categoryType(category.getCategoryType())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }


}