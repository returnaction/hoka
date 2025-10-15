package com.unioncoders.smartsupportbackend.controller;

import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import com.unioncoders.smartsupportbackend.model.SupportRequest;
import com.unioncoders.smartsupportbackend.service.*;
import com.unioncoders.smartsupportbackend.service.client.SciboxClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SupportController {

    //TODO сделай нормальный Response

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
    @PostMapping( value = "/faq/import",
            consumes = "multipart/form-data",
            produces = "application/json")
    public ResponseEntity<Map<String, Object>> importFaq(@RequestParam("file") MultipartFile file) {
        int inserted = importService.importFromExcel(file);
        return ResponseEntity.ok(Map.of("status", "ok", "inserted", inserted));
    }

    /// 2.1 делаем семантический поиск
    @PostMapping("/classify/semantic")
    public Map<String, Object> classifySemantic(@RequestBody SupportRequest req,
                                                @RequestParam(defaultValue = "3") int topK) {
        return searchService.classify(req.getText(), topK);
    }

    /// 2.B.1 подсказка для категории
    @PostMapping("/classify")
    public ResponseEntity<SciboxResponse> classify(@RequestBody SupportRequest request) {
        return ResponseEntity.ok(sciboxClient.classifyText(request.getText()));
    }

    /// 3.1 health check - делает легкий вызов  /v1/embeddings с  текстом ping и возвращает true/false;
    @GetMapping(value = "/health/scibox", produces = "application/json")
    public Map<String, Object> sciboxHealth() {
        boolean ok = sciboxClient.healthCheck();
        return Map.of("scibox", ok ? "ok" : "down");
    }



    /// возвращает эмбеддинги для текста чисто для дебага . не нужно
    @PostMapping("/embedding")
    public ResponseEntity<List<Double>> getEmbedding(@RequestBody SupportRequest request) {
        return ResponseEntity.ok(sciboxClient.getEmbedding(request.getText()));
    }

}