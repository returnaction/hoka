package com.unioncoders.smartsupportbackend.service;

import com.unioncoders.smartsupportbackend.repository.FaqEmbeddingsRepository;
import com.unioncoders.smartsupportbackend.service.client.SciboxClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SemanticSearchService {

    private final SciboxClient sciboxClient;
    private final FaqEmbeddingsRepository faqEmbeddingsRepository;

    public SemanticSearchService(SciboxClient sciboxClient, FaqEmbeddingsRepository faqEmbeddingsRepository) {
        this.sciboxClient = sciboxClient;
        this.faqEmbeddingsRepository = faqEmbeddingsRepository;
    }


    /// 2.2 Топ-K кандидатов по косинусной близости
    public List<Map<String, Object>> searchTopK(String query, int k) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        int topK = Math.max(1, Math.min(k, 10));
        List<Double> vec = sciboxClient.getEmbedding(query.trim());
        return faqEmbeddingsRepository.searchByEmbedding(vec, topK);
    }

    /// Упрощённая классификация: лучший кандидат → категория/подкатегория + кандидаты
    public Map<String, Object> classify(String query, int k) {
        var top = searchTopK(query, k);
        var best = top.isEmpty() ? Map.<String, Object>of() : top.get(0);
        return Map.of(
                "category",    best.getOrDefault("category", "UNKNOWN"),
                "subcategory", best.getOrDefault("subcategory", "UNKNOWN"),
                "candidates",  top
        );
    }

}
