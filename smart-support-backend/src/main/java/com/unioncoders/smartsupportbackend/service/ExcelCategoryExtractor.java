package com.unioncoders.smartsupportbackend.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.*;

@Service
public class ExcelCategoryExtractor {

    public Map<String, List<String>> extractCategories(String filePath) {
        Map<String, List<String>> categories = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // первая вкладка Excel
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // пропускаем заголовок

                Cell mainCategoryCell = row.getCell(0); // первая колонка
                Cell subCategoryCell = row.getCell(1);  // вторая колонка

                if (mainCategoryCell == null || subCategoryCell == null) continue;

                String mainCategory = mainCategoryCell.getStringCellValue().trim();
                String subCategory = subCategoryCell.getStringCellValue().trim();

                if (mainCategory.isEmpty() || subCategory.isEmpty()) continue;

                // добавляем в Map
                categories.computeIfAbsent(mainCategory, k -> new ArrayList<>());

                List<String> subList = categories.get(mainCategory);
                if (!subList.contains(subCategory)) {
                    subList.add(subCategory);
                }
            }

            System.out.println("✅ Извлечено категорий: " + categories.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categories;
    }
}
