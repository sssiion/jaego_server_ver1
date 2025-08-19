package org.example.jaego.Controller;


import lombok.RequiredArgsConstructor;
import org.example.jaego.Dto.*;

import org.example.jaego.Service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    // 모든 카테고리
    @GetMapping
    public List<CategoryDto> getAllCategories() {
        return categoryService.getAllCategories();
    }
    //카테고리 타입에 따른 반환
    @GetMapping("/category/{categoryType}")
    public List<CategoryDto> getAllCategoriesByCategoryType(@PathVariable String categoryType) {
        return categoryService.getCategoriesByType(categoryType);
    }
    @GetMapping("/with-count")
    public List<CategoryWithInventoryCountDto> getAllCategoriesWithCount() {
        return categoryService.getAllCategoriesWithInventoryCount();
    }

    @GetMapping("/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }
    @PostMapping("/update/{categoryType}")
    public ResponseEntity<?> updateCategory(@RequestBody List<Long> request, @PathVariable String categoryType) {
        try {
            categoryService.updateCategories(request, categoryType);
            return ResponseEntity.ok().build();
        } catch(Exception e) {
            // 에러 메시지를 response body로 전달
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
