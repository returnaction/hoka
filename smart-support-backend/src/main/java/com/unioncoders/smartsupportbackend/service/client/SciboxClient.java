package com.unioncoders.smartsupportbackend.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unioncoders.smartsupportbackend.config.SciboxProperties;
import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import com.unioncoders.smartsupportbackend.util.TaxonomyProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.core.type.TypeReference;

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
                    try { msg = StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8); } catch (Exception ignore) {}
                    throw new RuntimeException("SciBox HTTP %s: %s".formatted(res.getStatusCode(), msg));
                })
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    // 2.B Подсказка категории/подкатегории от LLM
    public SciboxResponse classifyText(String text) {
        String normalisedText = normaliseText(text);
        String systemPrompt = buildSystemPrompt(taxonomyProvider.getCategories());
        Map<String, Object> body = Map.of(
                "model", props.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user",   "content", normalisedText)
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
            SciboxResponse fb = new SciboxResponse();
            fb.setCategory("PARSE_ERROR");
            fb.setSubcategory(String.valueOf(res));
            return fb;
        }
    }

    // 2.3 Один эмбеддинг
    public List<Double> getEmbedding(String text) {
        Map<String, Object> res = postJson("/embeddings", Map.of(
                "model", props.getEmbedModel(),
                "input", text
        ));
        Map item = (Map) ((List) res.get("data")).get(0);
        List<?> raw = (List<?>) item.get("embedding");
        List<Double> vec = new ArrayList<>(raw.size());
        for (Object v : raw) vec.add(v instanceof Number n ? n.doubleValue() : Double.parseDouble(v.toString()));
        return vec;
    }

    // 1.4 Батч эмбеддингов
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
                for (Object v : raw) vec.add(v instanceof Number n ? n.doubleValue() : Double.parseDouble(v.toString()));
                all.add(vec);
            }
        }
        return all;
    }

    // 3.1 Health-check
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

    private static String buildSystemPrompt(Map<String, List<String>> taxonomy) {
        StringBuilder sb = new StringBuilder("Ты эксперт службы поддержки банка.\n")
                .append("Определи категорию и подкатегорию запроса клиента. Используй только список ниже:\n\n");
        taxonomy.forEach((cat, subs) -> {
            sb.append("Категория: ").append(cat).append("\n");
            for (String s : subs) sb.append("   - ").append(s).append("\n");
        });
        sb.append("\nОтвет верни строго в JSON виде: ")
                .append("{\"category\":\"<категория>\",\"subcategory\":\"<подкатегория>\"}");
        return sb.toString();
    }


    //нормализация вопроса(убрать мат, жаргон)
    public String normaliseText(String text) {
        String systemPrompt = String.join("\n",
                "Ты — аналитик клиентских обращений в банке.",
                "Твоя задача — переформулировать вопрос клиента, если он:",
                "— содержит эмоциональные выражения, жалобы или агрессию;",
                "— использует жаргон или разговорные фразы;",
                "Если вопрос уже нейтральный и понятный — верни его без изменений.",
                "Если требуется переформулировка — перепиши его строго в виде запроса, без обращения к клиенту, без извинений, без вопросов от лица поддержки.",
                "Просто верни переформулированный текст."
        );

        Map<String, Object> body = Map.of(
                "model", "Qwen2.5-72B-Instruct-AWQ",
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
                return (String) message.get("content");
            } else {
                return "Ответ не получен или пуст.";
            }
        } catch (Exception e) {
            return "Ошибка при обработке запроса: " + e.getMessage();
        }
    }

    //извлечение ключевых сущностей
    public List<String> retrieveEntities(String text) {
        String systemPrompt = String.join("\n",
                "Ты — помощник, извлекающий ключевые сущности из текста.",
                "Проанализируй вопрос пользователя и верни список сущностей в формате JSON-массива строк.",
                "Пример: [\"сущность1\", \"сущность2\", \"сущность3\"]"
        );

        Map<String, Object> body = Map.of(
                "model", "Qwen2.5-72B-Instruct-AWQ",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", text)
                ),
                "temperature", 0.5,
                "max_tokens", 256
        );

        try {
            Map<String, Object> res = postJson("/chat/completions", body);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(content, new TypeReference<List<String>>() {});
            } else {
                return List.of("Ответ не получен или пуст.");
            }
        } catch (Exception e) {
            return List.of("Ошибка при обработке запроса: " + e.getMessage());
        }
    }

}