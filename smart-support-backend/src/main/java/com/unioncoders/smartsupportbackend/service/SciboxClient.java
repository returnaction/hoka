package com.unioncoders.smartsupportbackend.service;

import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import com.unioncoders.smartsupportbackend.model.ChangedAnswerResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SciboxClient {



    @Value("${scibox.base-url}")
    private String baseUrl;

    @Value("${scibox.api-key}")
    private String apiKey;

    private final TaxonomyProvider taxonomyProvider;

    public SciboxClient(TaxonomyProvider taxonomyProvider) {
        this.taxonomyProvider = taxonomyProvider;
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public SciboxResponse classifyText(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, List<String>> taxonomy = taxonomyProvider.getCategories();

        // Собираем категории и подкатегории в строку
        StringBuilder promptBuilder = new StringBuilder("Ты эксперт службы поддержки банка.\n");
        promptBuilder.append("Определи категорию и подкатегорию запроса клиента. ")
                .append("Используй только список ниже:\n\n");

        taxonomy.forEach((category, subcategories) -> {
            promptBuilder.append("Категория: ").append(category).append("\n");
            subcategories.forEach(sub -> promptBuilder.append("   - ").append(sub).append("\n"));
        });

        promptBuilder.append("\nОтвет верни строго в JSON виде: ")
                .append("{\"category\": \"<категория>\", \"subcategory\": \"<подкатегория>\"}");

        String systemPrompt = promptBuilder.toString();

        Map<String, Object> body = Map.of(
                "model", "Qwen2.5-72B-Instruct-AWQ",
                "messages", new Object[]{
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", text)
                },
                "temperature", 0.3,
                "max_tokens", 256
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, request, Map.class);

        Map<String, Object> choices =
                (Map<String, Object>) ((Map) ((java.util.List) response.getBody().get("choices")).get(0)).get("message");
        String content = (String) choices.get("content");

        // 🧩 Попробуем распарсить JSON-ответ
        SciboxResponse result = new SciboxResponse();
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            SciboxResponse parsed = mapper.readValue(content, SciboxResponse.class);
            return parsed;
        } catch (Exception e) {
            // если не JSON — вернём как текст
            result.setCategory(content);
            result.setSubcategory("Не удалось определить подкатегорию");
            return result;
        }
    }

    public ChangedAnswerResponse normaliseText(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Собираем категории и подкатегории в строку
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Ты — аналитик клиентских обращений в банке.\n");
        promptBuilder.append("Твоя задача — переформулировать вопрос клиента, если он:\n");
        promptBuilder.append("— содержит эмоциональные выражения, жалобы или агрессию;\n");
        promptBuilder.append("— использует жаргон или разговорные фразы;\n");
        promptBuilder.append("Если вопрос уже нейтральный и понятный — верни его без изменений.\n");
        promptBuilder.append("Если требуется переформулировка — перепиши его строго в виде запроса, без обращения к клиенту, без извинений, без вопросов от лица поддержки.\n");
        promptBuilder.append("Просто верни переформулированный текст.");


        String systemPrompt = promptBuilder.toString();

        Map<String, Object> body = Map.of(
                "model", "Qwen2.5-72B-Instruct-AWQ",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", text)
                ),
                "temperature", 0.3,
                "max_tokens", 256
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ChangedAnswerResponse result = new ChangedAnswerResponse();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(baseUrl, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    String content = (String) message.get("content");
                    result.setText(content);
                } else {
                    result.setText("Ответ не получен или пуст.");
                }
            } else {
                result.setText("Ошибка при получении ответа от модели.");
            }
        } catch (Exception e) {
            result.setText("Ошибка при обработке запроса: " + e.getMessage());
        }

        return result;
    }


}
