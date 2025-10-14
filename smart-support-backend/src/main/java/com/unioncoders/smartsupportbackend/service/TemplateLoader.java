package com.unioncoders.smartsupportbackend.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.*;

@Service
public class TemplateLoader {

    public List<Map<String, String>> loadTemplates(String filePath) {
        List<Map<String, String>> templates = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // пропускаем заголовок

                Cell categoryCell = row.getCell(0); // A: Основная категория
                Cell subcategoryCell = row.getCell(1); // B: Подкатегория
                Cell questionCell = row.getCell(2); // C: Пример вопроса
                Cell priorityCell = row.getCell(3); // D: Приоритет
                Cell audienceCell = row.getCell(4); // E: Целевая аудитория
                Cell answerCell = row.getCell(5); // F: Шаблонный ответ

                // пропускаем пустые строки
                if (categoryCell == null || subcategoryCell == null || questionCell == null || answerCell == null)
                    continue;

                String category = categoryCell.getStringCellValue().trim();
                String subcategory = subcategoryCell.getStringCellValue().trim();
                String question = questionCell.getStringCellValue().trim();
                String priority = priorityCell != null ? priorityCell.getStringCellValue().trim() : "";
                String audience = audienceCell != null ? audienceCell.getStringCellValue().trim() : "";
                String answer = answerCell.getStringCellValue().trim();

                Map<String, String> record = new HashMap<>();
                record.put("category", category);
                record.put("subcategory", subcategory);
                record.put("question", question);
                record.put("priority", priority);
                record.put("audience", audience);
                record.put("answer", answer);

                templates.add(record);
            }

            System.out.println("✅ Загружено шаблонов: " + templates.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return templates;
    }
}
