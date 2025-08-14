package org.example.jaego.Service;



import org.example.jaego.Dto.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {

    // 카테고리별 재고 조회 (검색, 정렬 포함)
    List<InventoryDto> getInventoriesByCategory(Long categoryId, String searchKeyword);

    // 키워드로 전체 재고 검색
    List<InventorySearchDto> searchInventories(String keyword);

    // 개별 재고 상세 정보 조회
    InventoryDetailDto getInventoryDetails(Long inventoryId);

    // 새 재고 생성
    InventoryDto createInventory(InventoryCreateRequest request);

    // 재고 정보 수정
    InventoryDto updateInventory(Long inventoryId, InventoryUpdateRequest request);

    // 재고 삭제
    void deleteInventory(Long inventoryId);

    // 총 수량 업데이트
    void updateTotalQuantity(Long inventoryId);

    // 재고 요약 정보 조회
    InventorySummaryDto getInventorySummary(Long inventoryId);

    // 임박 배치가 있는 재고 조회
    List<UrgentInventoryDto> findInventoriesWithUrgentBatches(Integer days);

    // 최근 업데이트된 재고 조회
    List<InventoryDto> getRecentlyUpdatedInventories();

    // 재고 상세 조회 (기본)
    InventoryDto getInventoryById(Long inventoryId);

    // 모든 재고 조회
    Page<InventoryDto> getAllInventories(Pageable pageable);
}
