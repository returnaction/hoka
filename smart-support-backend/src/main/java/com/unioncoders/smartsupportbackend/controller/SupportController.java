package com.unioncoders.smartsupportbackend.controller;

import com.unioncoders.smartsupportbackend.model.*;
import com.unioncoders.smartsupportbackend.repository.EmbeddingRepository;
import com.unioncoders.smartsupportbackend.service.SciboxClient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/analyze")
public class SupportController {

    private final SciboxClient sciboxClient;
    private final EmbeddingRepository embeddingRepository;

    public SupportController(SciboxClient sciboxClient, EmbeddingRepository embeddingRepository) {
        this.sciboxClient = sciboxClient;
        this.embeddingRepository = embeddingRepository;
    }

    @PostMapping
    public ResponseEntity<SciboxResponse> classify(@RequestBody SupportRequest request) {
        SciboxResponse response = sciboxClient.classifyText(request.getText());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import/excel")
    public ResponseEntity<String> importFaq(@RequestParam("file") MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // пропускаем заголовок

                String category = row.getCell(0).getStringCellValue();
                String subcategory = row.getCell(1).getStringCellValue();
                String question = row.getCell(2).getStringCellValue();
                String answer = row.getCell(3).getStringCellValue();
                String priority = row.getCell(4).getStringCellValue();
                String audience = row.getCell(5).getStringCellValue();

                // Векторизация через SciBox
                List<Double> embedding = sciboxClient.getEmbedding(question);

                // Сохраняем напрямую в PostgreSQL через JDBC
                embeddingRepository.saveEmbedding(
                        category,
                        subcategory,
                        question,
                        answer,
                        priority,
                        audience,
                        embedding
                );
            }
        }

        return ResponseEntity.ok("Импорт FAQ успешно завершён ✅");
    }

    @PostMapping("/embedding")
    public ResponseEntity<List<Double>> getEmbedding(@RequestBody SupportRequest request) {
        List<Double> embedding = sciboxClient.getEmbedding(request.getText());
        return ResponseEntity.ok(embedding);
    }
}
