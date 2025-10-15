package com.unioncoders.smartsupportbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class SciboxClient {

    @Value("${scibox.chat-url}")
    private String chatUrl;

    @Value("${scibox.embed-url}")
    private String embedUrl;

    @Value("${scibox.api-key}")
    private String apiKey;

    private final TaxonomyProvider taxonomyProvider;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public SciboxClient(TaxonomyProvider taxonomyProvider) {
        this.taxonomyProvider = taxonomyProvider;
    }

    public SciboxResponse classifyText(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, List<String>> taxonomy = taxonomyProvider.getCategories();

        StringBuilder promptBuilder = new StringBuilder("Ты эксперт службы поддержки банка.\n");
        promptBuilder.append("Определи категорию и подкатегорию запроса клиента. ")
                .append("Используй только список ниже:\n\n");
        taxonomy.forEach((category, subs) -> {
            promptBuilder.append("Категория: ").append(category).append("\n");
            subs.forEach(sub -> promptBuilder.append("   - ").append(sub).append("\n"));
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

        // ⬇️ было baseUrl — должно быть chatUrl
        ResponseEntity<Map> response = restTemplate.postForEntity(chatUrl, request, Map.class);

        Map firstChoice = (Map) ((List) response.getBody().get("choices")).get(0);
        Map message = (Map) firstChoice.get("message");
        String content = (String) message.get("content");

        try {
            return mapper.readValue(content, SciboxResponse.class);
        } catch (Exception e) {
            SciboxResponse fallback = new SciboxResponse();
            fallback.setCategory(content);
            fallback.setSubcategory("Не удалось определить подкатегорию");
            return fallback;
        }
    }

    public List<Double> getEmbedding(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "model", "bge-m3",
                "input", text
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // ⬇️ используем embedUrl из настроек
        ResponseEntity<Map> response = restTemplate.postForEntity(embedUrl, request, Map.class);

        return (List<Double>) ((Map) ((List) response.getBody().get("data")).get(0)).get("embedding");
    }


    ///  выозов из FaqImposrtService
    public List<List<Double>> getEmbeddings(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) return List.of();

        // На всякий случай разобьём на батчи (ограничение по токенам/размеру запроса)
        final int BATCH_SIZE = 128;
        List<List<Double>> all = new java.util.ArrayList<>(inputs.size());

        for (int i = 0; i < inputs.size(); i += BATCH_SIZE) {
            int to = Math.min(i + BATCH_SIZE, inputs.size());
            List<String> batch = inputs.subList(i, to);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "model", "bge-m3",
                    "input", batch
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(embedUrl, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("SciBox embeddings failed: " + response.getStatusCode());
            }

            Object dataObj = response.getBody().get("data");
            if (!(dataObj instanceof List<?> dataList)) {
                throw new RuntimeException("Unexpected SciBox embeddings payload: no data array");
            }

            for (Object item : dataList) {
                if (!(item instanceof Map<?, ?> itemMap)) {
                    throw new RuntimeException("Unexpected embeddings item type");
                }
                Object emb = itemMap.get("embedding");
                if (!(emb instanceof List<?> embList)) {
                    throw new RuntimeException("Unexpected embedding type");
                }
                // Приводим к List<Double>
                List<Double> vec = new java.util.ArrayList<>(embList.size());
                for (Object v : embList) {
                    if (v instanceof Number n) vec.add(n.doubleValue());
                    else if (v instanceof String s) vec.add(Double.parseDouble(s));
                    else throw new RuntimeException("Non-numeric embedding value");
                }
                all.add(vec);
            }
        }

        return all;
    }
}
