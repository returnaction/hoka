package com.unioncoders.smartsupportbackend.controller;

import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import com.unioncoders.smartsupportbackend.model.SupportRequest;
import com.unioncoders.smartsupportbackend.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SupportController {

    private final SciboxClient sciboxClient;
    private final FaqImportService importService;
    private final SemanticSearchService searchService;

    public SupportController(SciboxClient sciboxClient,
                             FaqImportService importService,
                             SemanticSearchService searchService) {
        this.sciboxClient = sciboxClient;
        this.importService = importService;
        this.searchService = searchService;
    }

    /// 1.1 импорт Excel
    @PostMapping(value = "/faq/import", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importFaq(@RequestParam("file") MultipartFile file) {
        int inserted = importService.importFromExcel(file);
        return ResponseEntity.ok(Map.of("status", "ok", "inserted", inserted));
    }

    /// классифицирует текст обращения.
    @PostMapping("/classify")
    public ResponseEntity<SciboxResponse> classify(@RequestBody SupportRequest request) {
        return ResponseEntity.ok(sciboxClient.classifyText(request.getText()));
    }

    /// возвращает эмбеддинги для текста
    @PostMapping("/embedding")
    public ResponseEntity<List<Double>> getEmbedding(@RequestBody SupportRequest request) {
        return ResponseEntity.ok(sciboxClient.getEmbedding(request.getText()));
    }

    /// семантический поиск по БЗ.
    @PostMapping("/faq/search")
    public ResponseEntity<List<Map<String, Object>>> semanticSearch(@RequestBody SupportRequest request) {
        return ResponseEntity.ok(searchService.searchTopK(request.getText(), 3));
    }
}