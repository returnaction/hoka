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

    public SemanticSearchService(SciboxClient sciboxClient,
                                 FaqEmbeddingsRepository faqEmbeddingsRepository) {
        this.sciboxClient = sciboxClient;
        this.faqEmbeddingsRepository = faqEmbeddingsRepository;
    }

    /**
     * Топ-K кандидатов по косинусной близости по обогащённому запросу.
     */
    public List<Map<String, Object>> searchTopK(String query, int k) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        int topK = Math.max(1, Math.min(k, 10));
        List<Double> vec = sciboxClient.getEmbedding(query.trim());
        return faqEmbeddingsRepository.searchByEmbedding(vec, topK);
    }

    /**
     * Упрощённая классификация: лучший кандидат -> категория/подкатегория + кандидаты.
     */
    public Map<String, Object> classify(String query, int k) {
        var top = searchTopK(query, k);
        var best = top.isEmpty() ? Map.<String, Object>of() : top.get(0);
        return Map.of(
                "category",    best.getOrDefault("category", "UNKNOWN"),
                "subcategory", best.getOrDefault("subcategory", "UNKNOWN"),
                "candidates",  top
        );
    }

    /**
     * Гибридный пайплайн:
     * 1) нормализация → 2) сущности → 3) обогащение → 4) LLM-категоризация → 5) семантический поиск
     * При низкой уверенности — один ретрай с перефразом.
     */
    public Map<String, Object> hybridClassify(String text, int topK, double threshold) {
        if (text == null || text.isBlank()) {
            return Map.of("Ошибка", "Текст не может быть пустым");
        }

        // Нормализуем параметры
        int k = Math.max(1, Math.min(topK, 10));
        double thr = Math.max(0.0, Math.min(threshold, 1.0));

        // 1) Нормализация
        String normalised = sciboxClient.normaliseText(text);

        // 2) Сущности
        List<String> entities = sciboxClient.retrieveEntities(normalised);

        // 3) Обогащение
        String enriched = sciboxClient.enrichQuery(normalised, entities);

        // 4) LLM-категоризация (используем prepared, чтобы не нормализовать повторно внутри)
        var llm = sciboxClient.classifyPrepared(enriched);

        // 5) Семантический поиск (по обогащённому запросу)
        List<Map<String, Object>> candidates = searchTopK(enriched, Math.max(2, k));

        // 6) Грубая уверенность по семантике (margin top1 - top2)
        double confidence = estimateConfidenceFromCandidates(candidates);

        boolean usedRephrase = false;

        // Если уверенность ниже порога — один ретрай: перефраз → повтор шагов 2–5
        if (confidence < thr) {
            String reformulated = sciboxClient.changeQuestionToSimilarText(enriched);
            usedRephrase = true;

            entities = sciboxClient.retrieveEntities(reformulated);
            enriched = sciboxClient.enrichQuery(reformulated, entities);
            llm = sciboxClient.classifyPrepared(enriched);
            candidates = searchTopK(enriched, Math.max(2, k));
            confidence = estimateConfidenceFromCandidates(candidates);
        }

        // Безопасно достаём итоговые поля LLM (на случай PARSE_ERROR)
        String finalCategory = (llm != null && llm.getCategory() != null) ? llm.getCategory() : "UNKNOWN";
        String finalSubcat   = (llm != null && llm.getSubcategory() != null) ? llm.getSubcategory() : "UNKNOWN";

        // Итоговый ответ для фронта (ключи на русском)
        return Map.of(
                "ввод", text,
                "нормализованный", normalised,
                "сущности", entities,
                "обогащённыйЗапрос", enriched,
                "llm", Map.of(
                        "категория", finalCategory,
                        "подкатегория", finalSubcat
                ),
                "семантика", Map.of(
                        "кандидаты", candidates == null ? List.of() : candidates,
                        "уверенность", confidence,
                        "порог", thr
                ),
                "решение", Map.of(
                        "использованПерефраз", usedRephrase,
                        "итоговаяКатегория", finalCategory,
                        "итоговаяПодкатегория", finalSubcat
                )
        );
    }

    // --- Вспомогательные методы ---

    /**
     * Простая метрика уверенности: разница между оценками top1 и top2.
     * Если кандидат один — берём его оценку. Если нет кандидатов — 0.
     */
    private double estimateConfidenceFromCandidates(List<Map<String, Object>> cands) {
        if (cands == null || cands.isEmpty()) return 0.0;
        double s1 = extractScore(cands.get(0));
        if (cands.size() == 1) return s1;
        double s2 = extractScore(cands.get(1));
        return Math.max(0.0, s1 - s2);
        // Альтернатива: вернуть просто s1, если хотите "абсолютную" уверенность топ-1.
    }

    /**
     * Достаём числовую «оценку схожести» кандидата.
     * Подстрой названия ключа под то, что возвращает репозиторий.
     */
    private double extractScore(Map<String, Object> m) {
        // возможные ключи: "score", "similarity", "cosine"
        Object v = m.get("score");
        if (v == null) v = m.get("similarity");
        if (v == null) v = m.get("cosine");
        if (v instanceof Number n) return n.doubleValue();
        if (v != null) {
            try { return Double.parseDouble(v.toString()); } catch (Exception ignore) {}
        }
        return 0.0;
    }
}