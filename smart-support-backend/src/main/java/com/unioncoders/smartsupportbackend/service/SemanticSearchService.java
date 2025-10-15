package com.unioncoders.smartsupportbackend.service;

import com.unioncoders.smartsupportbackend.repository.FaqEmbeddingsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SemanticSearchService {

    private final SciboxClient sciboxClient;
    private final FaqEmbeddingsRepository repository;

    public SemanticSearchService(SciboxClient sciboxClient, FaqEmbeddingsRepository repository) {
        this.sciboxClient = sciboxClient;
        this.repository = repository;
    }

    public List<Map<String, Object>> searchTopK(String query, int k) {
        var emb = sciboxClient.getEmbedding(query);
       // return repository.searchByEmbedding(emb, k); //TODO исправить так как изменили repostiories
        return null;
    }
}
