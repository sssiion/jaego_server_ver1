package org.example.jaego.Dto;

import lombok.*;

import java.util.List;

// 엑셀 처리 결과
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelProcessResultDto {
    private String processingType;   // "재고" 또는 "발주"
    private Integer totalRows;       // 전체 행 수
    private Integer processedRows;   // 처리된 행 수
    private Integer errorRows;       // 오류 행 수
    private List<String> errorMessages; // 오류 메시지들
    private List<String> processedProducts; // 처리된 상품 목록
    private String processingTime;   // 처리 시간
}
