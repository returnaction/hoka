package com.unioncoders.smartsupportbackend.service.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unioncoders.smartsupportbackend.config.SciboxProperties;
import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import com.unioncoders.smartsupportbackend.util.TaxonomyProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class SciboxClient {

    private final RestClient client;
    private final SciboxProperties props;
    private final TaxonomyProvider taxonomyProvider;
    private final ObjectMapper mapper = new ObjectMapper();

    public SciboxClient(RestClient sciboxRestClient,
                        SciboxProperties props,
                        TaxonomyProvider taxonomyProvider) {
        this.client = sciboxRestClient;
        this.props = props;
        this.taxonomyProvider = taxonomyProvider;
    }

    private Map<String, Object> postJson(String path, Map<String, Object> body) {
        return client.post()
                .uri(path)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String msg = "";
                    try {
                        msg = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8);
                    } catch (Exception ignore) {}
                    throw new RuntimeException("SciBox HTTP %s: %s".formatted(res.getStatusCode(), msg));
                })
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    // --- КЛАССИФИКАЦИЯ ---

    /**
     * Быстрый путь: принимает сырой текст,
     * сам нормализует его и отправляет в классификатор.
     */
    public SciboxResponse classifyText(String rawText) {
        String normalisedText = normaliseText(rawText);
        return classifyPrepared(normalisedText);
    }

    /**
     * Принимает уже подготовленный (нормализованный/обогащённый) текст.
     * Возвращает JSON {"category":"...","subcategory":"..."} (и др., если расширить модель ответа).
     */
    public SciboxResponse classifyPrepared(String preparedText) {
        String systemPrompt = buildSystemPrompt(taxonomyProvider.getCategories());
        Map<String, Object> body = Map.of(
                "model", props.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user",   "content", preparedText)
                ),
                "temperature", 0.3,
                "max_tokens", 256
        );
        Map<String, Object> res = postJson("/chat/completions", body);

        try {
            Map firstChoice = (Map) ((List) res.get("choices")).get(0);
            Map message = (Map) firstChoice.get("message");
            String content = (String) message.get("content");
            return mapper.readValue(content, SciboxResponse.class);
        } catch (Exception e) {
            // Безопасный fallback: вернём признак парс-ошибки и "сырое" содержимое
            SciboxResponse fb = new SciboxResponse();
            fb.setCategory("PARSE_ERROR");
            fb.setSubcategory(String.valueOf(res));
            return fb;
        }
    }

    // --- ЭМБЕДДИНГИ ---

    /** Одиночный эмбеддинг. */
    public List<Double> getEmbedding(String text) {
        Map<String, Object> res = postJson("/embeddings", Map.of(
                "model", props.getEmbedModel(),
                "input", text
        ));
        Map item = (Map) ((List) res.get("data")).get(0);
        List<?> raw = (List<?>) item.get("embedding");
        List<Double> vec = new ArrayList<>(raw.size());
        for (Object v : raw) {
            vec.add(v instanceof Number n ? n.doubleValue() : Double.parseDouble(v.toString()));
        }
        return vec;
    }

    /** Батч эмбеддингов (разбивка на части внутри). */
    public List<List<Double>> getEmbeddings(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) return List.of();
        final int BATCH = 128;
        List<List<Double>> all = new ArrayList<>(inputs.size());

        for (int i = 0; i < inputs.size(); i += BATCH) {
            List<String> part = inputs.subList(i, Math.min(i + BATCH, inputs.size()));
            Map<String, Object> res = postJson("/embeddings", Map.of(
                    "model", props.getEmbedModel(),
                    "input", part
            ));
            List<?> data = (List<?>) res.get("data");
            for (Object it : data) {
                Map item = (Map) it;
                List<?> raw = (List<?>) item.get("embedding");
                List<Double> vec = new ArrayList<>(raw.size());
                for (Object v : raw) {
                    vec.add(v instanceof Number n ? n.doubleValue() : Double.parseDouble(v.toString()));
                }
                all.add(vec);
            }
        }
        return all;
    }

    // --- HEALTH CHECK ---

    /** Лёгкая проверка доступности сервиса эмбеддингов. */
    public boolean healthCheck() {
        try {
            Map<String, Object> res = postJson("/embeddings", Map.of(
                    "model", props.getEmbedModel(),
                    "input", List.of("ping")
            ));
            Object data = res.get("data");
            return (data instanceof List<?> list) && !list.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // --- ПРОМПТЫ И ПРЕ-/ПОСТ-ОБРАБОТКА ---

    /** Инструкция для классификации по таксономии (строго JSON). */
    private static String buildSystemPrompt(Map<String, List<String>> taxonomy) {
        StringBuilder sb = new StringBuilder("Ты эксперт службы поддержки банка.\n")
                .append("Определи категорию и подкатегорию запроса клиента. Используй ТОЛЬКО список ниже.\n\n");
        taxonomy.forEach((cat, subs) -> {
            sb.append("Категория: ").append(cat).append("\n");
            for (String s : subs) sb.append("  - ").append(s).append("\n");
        });
        // При желании можно попросить модель вернуть confidence (и расширить SciboxResponse)
        sb.append("\nВерни строго JSON вида: ")
                .append("{\"category\":\"<категория>\",\"subcategory\":\"<подкатегория>\"}");
        return sb.toString();
    }

    /**
     * Нормализация текста (убрать мат/жаргон/эмоции).
     * В случае ошибки/пустого ответа возвращаем исходный текст (безопасный fallback).
     */
    public String normaliseText(String text) {
        String systemPrompt = String.join("\n",
                "Ты — аналитик клиентских обращений в банке.",
                "Если вопрос уже нейтральный и понятный — верни его без изменений.",
                "Если требуется переформулировка — перепиши строго в виде запроса:",
                "- без обращения к клиенту,",
                "- без извинений,",
                "- без вопросов от лица поддержки.",
                "Просто верни переформулированный текст."
        );

        Map<String, Object> body = Map.of(
                "model", props.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", text)
                ),
                "temperature", 0.3,
                "max_tokens", 256
        );

        try {
            Map<String, Object> res = postJson("/chat/completions", body);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                return (content == null || content.isBlank()) ? text : content.trim();
            }
            return text;
        } catch (Exception e) {
            return text; // безопасный fallback
        }
    }

    /**
     * Извлечение ключевых сущностей. Требуем строго JSON-массив строк.
     * В случае ошибки возвращаем пустой список.
     */
    public List<String> retrieveEntities(String text) {
        String systemPrompt = String.join("\n",
                "Ты — помощник, извлекающий ключевые сущности из текста.",
                "Верни строго JSON-массив строк БЕЗ дополнительного текста.",
                "Пример: [\"сущность1\", \"сущность2\"]",
                "Никаких пояснений/Markdown."
        );

        Map<String, Object> body = Map.of(
                "model", props.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", text)
                ),
                "temperature", 0.4,
                "max_tokens", 256
        );

        try {
            Map<String, Object> res = postJson("/chat/completions", body);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                if (content == null || content.isBlank()) return Collections.emptyList();

                // Подстраховка: если модель «болтает», попробуем вырезать JSON-массив
                String trimmed = content.trim();
                int l = trimmed.indexOf('[');
                int r = trimmed.lastIndexOf(']');
                if (l >= 0 && r > l) {
                    trimmed = trimmed.substring(l, r + 1);
                }
                return mapper.readValue(trimmed, new TypeReference<List<String>>() {});
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList(); // безопасный fallback
        }
    }

    /**
     * Перефразировать вопрос, если классификация неуверенная.
     * В случае ошибки возвращаем исходный текст.
     */
    public String changeQuestionToSimilarText(String text) {
        String systemPrompt = "Переформулируй вопрос другими словами для дальнейшей классификации, сохранив смысл.";

        Map<String, Object> body = Map.of(
                "model", props.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", text)
                ),
                "temperature", 0.4,
                "max_tokens", 256
        );

        try {
            Map<String, Object> res = postJson("/chat/completions", body);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                return (content == null || content.isBlank()) ? text : content.trim();
            } else {
                return text;
            }
        } catch (Exception e) {
            return text;
        }
    }

    /**
     * Собрать «обогащённый» запрос: нормализованный текст + сущности.
     * Если сущностей нет — возвращаем только базовый текст.
     * Ограничиваем длину хвоста с сущностями.
     */
    public String enrichQuery(String normalisedText, List<String> retrievedEntities) {
        String base = (normalisedText == null ? "" : normalisedText.trim());
        if (retrievedEntities == null || retrievedEntities.isEmpty()) return base;

        String joined = String.join(", ", retrievedEntities);
        if (joined.length() > 300) joined = joined.substring(0, 300);

        return base.isEmpty()
                ? ("Ключевые сущности: " + joined)
                : (base + " | Ключевые сущности: " + joined);
    }
}