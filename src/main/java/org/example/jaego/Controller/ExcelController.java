package org.example.jaego.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.jaego.Dto.ExcelProcessResultDto;
import org.example.jaego.Service.ExcelProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
@Slf4j
public class ExcelController {

    private final ExcelProcessingService service;

    @CrossOrigin(origins = "*")
    @PostMapping("/upload")
    public ResponseEntity<ExcelProcessResultDto> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return switch (type) {
            case "재고" -> ResponseEntity.ok(service.processStockExcel(file));
            case "발주" -> ResponseEntity.ok(service.processOrderExcel(file));
            default -> ResponseEntity.badRequest().build();
        };

    }

    @GetMapping("/guide/{type}")
    public ResponseEntity<String> guide(@PathVariable String type) {
        if ("재고".equals(type)) {
            return ResponseEntity.ok("""
                📋 재고 엑셀 파일:
                1열: 상품명
                5열: 판매수량
                11열: 총수량
                첫 행은 헤더이므로 제외
                """);
        } else if ("발주".equals(type)) {
            return ResponseEntity.ok("""
                📋 발주 엑셀 파일:
                1열: 중분류(카테고리)
                2열: 상품명
                5열: 발주수량
                첫 행은 헤더이므로 제외
                """);
        }
        return ResponseEntity.badRequest().build();
    }
}
