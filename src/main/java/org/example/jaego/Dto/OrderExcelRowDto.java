package org.example.jaego.Dto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderExcelRowDto {
    private String categoryName;     // 카테고리명
    private String productName;      // 상품명
    private Integer orderQuantity;   // 발주수량(추가 수량)
    private LocalDateTime expiryDate;    // 유통기한 (선택사항)

    private boolean isValid = true;
    private String errorMessage;
}
