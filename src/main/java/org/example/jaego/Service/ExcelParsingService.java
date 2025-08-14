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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelParsingService {

    public List<StockExcelRowDto> parseStockExcel(MultipartFile file) {
        List<StockExcelRowDto> stockRows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 0행은 헤더
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;
                StockExcelRowDto dto = new StockExcelRowDto();
                dto.setProductName(getString(row.getCell(0)));
                dto.setSalesQuantity(getInt(row.getCell(4)));
                dto.setRemainingStock(getInt(row.getCell(10)));
                stockRows.add(dto);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("엑셀 파일을 읽을 수 없습니다: " + e.getMessage());
        }
        return stockRows;
    }

    public List<OrderExcelRowDto> parseOrderExcel(MultipartFile file) {
        List<OrderExcelRowDto> orderRows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;
                OrderExcelRowDto dto = new OrderExcelRowDto();
                dto.setCategoryName(getString(row.getCell(0)));
                dto.setProductName(getString(row.getCell(1)));
                dto.setOrderQuantity(getInt(row.getCell(4)));
                orderRows.add(dto);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("엑셀 파일을 읽을 수 없습니다: " + e.getMessage());
        }
        return orderRows;
    }

    private String getString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private Integer getInt(Cell cell) {
        if (cell == null) return 0;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> Integer.parseInt(cell.getStringCellValue().replace(",", ""));
            default -> 0;
        };
    }

    private boolean isEmptyRow(Row row) {
        return getString(row.getCell(0)).isEmpty();
    }
}
