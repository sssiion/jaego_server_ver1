package org.example.jaego.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.jaego.Dto.OrderExcelRowDto;
import org.example.jaego.Dto.StockExcelRowDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelParsingService {

    // 재고(출고) 엑셀 파싱
    public List<StockExcelRowDto> parseStockExcel(MultipartFile file) {
        List<StockExcelRowDto> stockRows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 헤더 행 확인
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("엑셀 파일에 헤더가 없습니다.");
            }

            log.info("재고 엑셀 파싱 시작 - 총 {}행", sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;

                StockExcelRowDto stockRow = parseStockRow(row, i);
                stockRows.add(stockRow);
            }

            log.info("재고 엑셀 파싱 완료 - {}행 처리", stockRows.size());

        } catch (IOException e) {
            log.error("엑셀 파일 파싱 오류", e);
            throw new IllegalArgumentException("엑셀 파일을 읽을 수 없습니다: " + e.getMessage());
        }

        return stockRows;
    }

    // 발주(입고) 엑셀 파싱
    public List<OrderExcelRowDto> parseOrderExcel(MultipartFile file) {
        List<OrderExcelRowDto> orderRows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("엑셀 파일에 헤더가 없습니다.");
            }

            log.info("발주 엑셀 파싱 시작 - 총 {}행", sheet.getLastRowNum());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;

                OrderExcelRowDto orderRow = parseOrderRow(row, i);
                orderRows.add(orderRow);
            }

            log.info("발주 엑셀 파싱 완료 - {}행 처리", orderRows.size());

        } catch (IOException e) {
            log.error("엑셀 파일 파싱 오류", e);
            throw new IllegalArgumentException("엑셀 파일을 읽을 수 없습니다: " + e.getMessage());
        }

        return orderRows;
    }

    // 재고 행 파싱
    private StockExcelRowDto parseStockRow(Row row, int rowIndex) {
        StockExcelRowDto stockRow = new StockExcelRowDto();

        try {
            // A열: 상품명
            Cell productNameCell = row.getCell(0);
            if (productNameCell != null) {
                stockRow.setProductName(getCellStringValue(productNameCell).trim());
            }

            // B열: 판매수량
            Cell salesQuantityCell = row.getCell(1);
            if (salesQuantityCell != null) {
                stockRow.setSalesQuantity(getCellIntegerValue(salesQuantityCell));
            }

            // C열: 남은재고수량(총수량)
            Cell remainingStockCell = row.getCell(2);
            if (remainingStockCell != null) {
                stockRow.setRemainingStock(getCellIntegerValue(remainingStockCell));
            }

            // 데이터 검증
            validateStockRow(stockRow, rowIndex);

        } catch (Exception e) {
            stockRow.setValid(false);
            stockRow.setErrorMessage(String.format("행 %d 파싱 오류: %s", rowIndex + 1, e.getMessage()));
            log.warn("재고 엑셀 행 {} 파싱 실패: {}", rowIndex + 1, e.getMessage());
        }

        return stockRow;
    }

    // 발주 행 파싱
    private OrderExcelRowDto parseOrderRow(Row row, int rowIndex) {
        OrderExcelRowDto orderRow = new OrderExcelRowDto();

        try {
            // A열: 카테고리
            Cell categoryCell = row.getCell(0);
            if (categoryCell != null) {
                orderRow.setCategoryName(getCellStringValue(categoryCell).trim());
            }

            // B열: 상품명
            Cell productNameCell = row.getCell(1);
            if (productNameCell != null) {
                orderRow.setProductName(getCellStringValue(productNameCell).trim());
            }

            // C열: 발주수량
            Cell orderQuantityCell = row.getCell(2);
            if (orderQuantityCell != null) {
                orderRow.setOrderQuantity(getCellIntegerValue(orderQuantityCell));
            }

            // D열: 유통기한 (선택사항)
            Cell expiryDateCell = row.getCell(3);
            if (expiryDateCell != null && getCellStringValue(expiryDateCell).trim().length() > 0) {
                orderRow.setExpiryDate(getCellDateValue(expiryDateCell));
            }

            // 데이터 검증
            validateOrderRow(orderRow, rowIndex);

        } catch (Exception e) {
            orderRow.setValid(false);
            orderRow.setErrorMessage(String.format("행 %d 파싱 오류: %s", rowIndex + 1, e.getMessage()));
            log.warn("발주 엑셀 행 {} 파싱 실패: {}", rowIndex + 1, e.getMessage());
        }

        return orderRow;
    }

    // 셀 값 추출 유틸리티 메서드들
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return 0;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("숫자 형식이 올바르지 않습니다: " + cell.getStringCellValue());
                }
            default:
                return 0;
        }
    }

    private LocalDate getCellDateValue(Cell cell) {
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            // 문자열 날짜 파싱 로직 추가 가능
            String dateStr = cell.getStringCellValue().trim();
            if (dateStr.isEmpty()) return null;

            try {
                // "2025-12-31" 형식 파싱
                return LocalDate.parse(dateStr);
            } catch (Exception e) {
                throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다: " + dateStr);
            }
        }

        return null;
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !getCellStringValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // 재고 데이터 검증
    private void validateStockRow(StockExcelRowDto stockRow, int rowIndex) {
        if (stockRow.getProductName() == null || stockRow.getProductName().trim().isEmpty()) {
            stockRow.setValid(false);
            stockRow.setErrorMessage(String.format("행 %d: 상품명이 비어있습니다.", rowIndex + 1));
            return;
        }

        if (stockRow.getSalesQuantity() == null || stockRow.getSalesQuantity() < 0) {
            stockRow.setValid(false);
            stockRow.setErrorMessage(String.format("행 %d: 판매수량이 올바르지 않습니다.", rowIndex + 1));
            return;
        }

        if (stockRow.getRemainingStock() == null || stockRow.getRemainingStock() < 0) {
            stockRow.setValid(false);
            stockRow.setErrorMessage(String.format("행 %d: 남은재고수량이 올바르지 않습니다.", rowIndex + 1));
            return;
        }
    }

    // 발주 데이터 검증
    private void validateOrderRow(OrderExcelRowDto orderRow, int rowIndex) {
        if (orderRow.getProductName() == null || orderRow.getProductName().trim().isEmpty()) {
            orderRow.setValid(false);
            orderRow.setErrorMessage(String.format("행 %d: 상품명이 비어있습니다.", rowIndex + 1));
            return;
        }

        if (orderRow.getOrderQuantity() == null || orderRow.getOrderQuantity() <= 0) {
            orderRow.setValid(false);
            orderRow.setErrorMessage(String.format("행 %d: 발주수량이 올바르지 않습니다.", rowIndex + 1));
            return;
        }

        // 카테고리는 선택사항이지만 있다면 비어있으면 안됨
        if (orderRow.getCategoryName() != null && orderRow.getCategoryName().trim().isEmpty()) {
            orderRow.setCategoryName(null); // 빈 문자열을 null로 변경
        }
    }
}