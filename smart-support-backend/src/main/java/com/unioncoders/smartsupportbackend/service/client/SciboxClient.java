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


    //перефразировать вопрос, когда не смогли в категорию добавить
    public String  changeQuestionToSimilarText(String text) {
        String systemPrompt = "Переформулируй вопрос для дальнейшей классификации другими словами, чтобы суть осталась та же\n";

        Map<String, Object> body = Map.of(
                "model", "Qwen2.5-72B-Instruct-AWQ",
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
                return (String) message.get("content");
            } else {
                return "Ответ не получен или пуст.";
            }
        } catch (Exception e) {
            return "Ошибка при обработке запроса: " + e.getMessage();
        }
    }

    //собираем enriched query
    public String enrichQuery(String normalisedText, List<String> retrievedEntities){
        String enrichedQuery = normalisedText + " Ключевые сущности: " + String.join(", ", retrievedEntities);
        return enrichedQuery;
    }
/*

//вычисление центроида
public static List<Double> computeCentroid(List<List<Double>> vectors) {
    int dim = vectors.get(0).size();
    List<Double> centroid = new ArrayList<>(Collections.nCopies(dim, 0.0));

    for (List<Double> vec : vectors) {
        for (int i = 0; i < dim; i++) {
            centroid.set(i, centroid.get(i) + vec.get(i));
        }
    }

    for (int i = 0; i < dim; i++) {
        centroid.set(i, centroid.get(i) / vectors.size());
    }

    return centroid;
}

//косинусное сходство
public static double cosineSimilarity(List<Double> a, List<Double> b) {
    double dot = 0.0, normA = 0.0, normB = 0.0;
    for (int i = 0; i < a.size(); i++) {
        dot += a.get(i) * b.get(i);
        normA += a.get(i) * a.get(i);
        normB += b.get(i) * b.get(i);
    }
    return dot / (Math.sqrt(normA) * Math.sqrt(normB));
}


//пришлось добавить, чтобы бесконечного цикла не было
public String resolveCategoryWithEmbeddings(
        String enrichedQuestion,
        List<Double> embeddedQuestion,
        Map<String, List<List<Double>>> embeddingsByCategory
) {
    return resolveCategoryWithEmbeddings(enrichedQuestion, embeddedQuestion, embeddingsByCategory, 0);
}

private String resolveCategoryWithEmbeddings(
        String enrichedQuestion,
        List<Double> embeddedQuestion,
        Map<String, List<List<Double>>> embeddingsByCategory,
        double threshold,
        int retryCount
) {
    final int MAX_RETRIES = 3; // максимум 3 попытки
    Map<String, Double> similarities = new HashMap<>();
//считаем центроиды по категориям
    for (Map.Entry<String, List<List<Double>>> entry : embeddingsByCategory.entrySet()) {
        String category = entry.getKey();
        List<List<Double>> vectors = entry.getValue();
        List<Double> centroid = computeCentroid(vectors);
        double similarity = cosineSimilarity(embeddedQuestion, centroid);
        similarities.put(category, similarity);
    }
//сортируем
    List<Map.Entry<String, Double>> sorted = similarities.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .toList();

    if (sorted.isEmpty()) {
        return "";
    }
//ищем топ 2
    Map.Entry<String, Double> top1 = sorted.get(0);
    Map.Entry<String, Double> top2 = sorted.size() > 1 ? sorted.get(1) : null;
    double SIMILARITY_DIFF_THRESHOLD = 0.03;
    double diff = (top2 != null) ? Math.abs(top1.getValue() - top2.getValue()) : 1.0;

//возвращаем с большей схожестью, если они не приблизительно равны
    if (diff >= SIMILARITY_DIFF_THRESHOLD) {
        return top1.getKey();
    }

    // Запрос к LLM, если близкие категории
    String systemPrompt = String.format("""
        Тебе нужно отнести данный вопрос: "%s"
        к одной из данных категорий: %s, %s.
        Формат ответа: категория, score
        """, enrichedQuestion, top1.getKey(), top2.getKey());

    Map<String, Object> body = Map.of(
            "model", "Qwen2.5-72B-Instruct-AWQ",
            "messages", List.of(Map.of("role", "user", "content", systemPrompt)),
            "temperature", 0.3,
            "max_tokens", 100
    );

    try {
        Map<String, Object> res = postJson("/chat/completions", body);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");

        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = ((String) message.get("content")).trim();

            String[] parts = content.split(",");
            if (parts.length >= 1) {
                String category = parts[0].trim();
                int score = 100;
                if (parts.length > 1) {
                    try {
                        score = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException ignored) {}
                }

                //проверяем, достаточна ли точность и сколько раз мы уже перефразировали, чтобы в бесконечный цикл не впасть
                if (score < 70 && retryCount < MAX_RETRIES) {
                    String rephrasedQuestion = changeQuestionToSimilarText(enrichedQuestion);
                    List<String> newEntities = retrieveEntities(rephrased);
                    String rephrasedEnrichedQuery = enrichQuery(rephrasedQuestion, newEntities);
                    List<Double> newEmbeddedQuestion = getEmbedding(rephrasedEnrichedQuery);
                    return resolveCategoryWithEmbeddings(
                            rephrasedEnrichedQuery,
                            embeddedQuestion, // можно пересчитать embedding, если нужно
                            embeddingsByCategory,
                            retryCount + 1
                    );
                }

                return category;
            }
        }
        return "";
    } catch (Exception e) {
        e.printStackTrace();
        return "";
    }
}



*********************


//поиск по
public String findMostSimilarQuestion(
        String enrichedQuestion,
        List<Double> embeddedQuestion,
        Map<String, List<Double>> questionEmbeddings,
        double threshold
) {
    // Считаем сходства для всех вопросов
    Map<String, Double> similarities = new HashMap<>();
    for (Map.Entry<String, List<Double>> entry : questionEmbeddings.entrySet()) {
        double sim = cosineSimilarity(embeddedQuestion, entry.getValue());
        similarities.put(entry.getKey(), sim);
    }

    // 2Сортируем по убыванию сходства
    List<Map.Entry<String, Double>> sorted = similarities.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .toList();

    if (sorted.isEmpty()) {
        return "";
    }

    Map.Entry<String, Double> top1 = sorted.get(0);
    double bestSim = top1.getValue();

    // Если сходство выше порога — возвращаем лучший результат
    if (bestSim >= threshold) {
        return top1.getKey();
    }

    // Иначе подключаем LLM для уточнения
    // Берём топ-3 вопроса для контекста
    List<String> topQuestions = sorted.stream()
            .limit(3)
            .map(Map.Entry::getKey)
            .toList();

    String systemPrompt = String.join("\n",
            "Ты — помощник, который определяет, какой из приведённых шаблонных вопросов ближе всего по смыслу к пользовательскому запросу.",
            "Вот пользовательский запрос: " + enrichedQuestion,
            "Вот варианты шаблонных вопросов:",
            String.join("\n", topQuestions),
            "Выбери один, который наиболее точно соответствует по смыслу, и верни его текст без пояснений."
    );

    try {
        Map<String, Object> body = Map.of(
                "model", "Qwen2.5-72B-Instruct-AWQ",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt)
                ),
                "temperature", 0.3,
                "max_tokens", 128
        );

        Map<String, Object> res = postJson("/chat/completions", body);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");

        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = ((String) message.get("content")).trim();

            // Если LLM ответил одним из шаблонных вопросов — возвращаем его
            for (String q : topQuestions) {
                if (content.toLowerCase().contains(q.toLowerCase())) {
                    return q;
                }
            }


            return top1.getKey();
        }

        return top1.getKey();

    } catch (Exception e) {
        return top1.getKey();
    }
}



public String retrieveTheQuestion(String question){

    String normalisedQuestion = normaliseText(question);
    List<String> entities = retrieveEntities(normalisedQuestion);
    String enrichedQuery = enrichQuery(normalisedQuestion, entities);
    List<Double> embeddedQuestion =  getEmbedding(enrichedQuery );

    Map<String, List<List<Double>>> EmbeddingsGrouppedByCategories = getEmbeddingsGroupedByCategory();

    String category = resolveCategoryWithEmbeddings(enrichedQuery, embeddedQuestion, EmbeddingsGrouppedByCategories);
    if (category!=""){
        Map<String, List<List<Double>>> EmbeddingsGrouppedBySubcategories = getEmbeddingsGroupedBySubcategory(category);
        String subcategory = resolveCategoryWithEmbeddings(enrichedQuery, embeddedQuestion, EmbeddingsGrouppedBySubcategories);
        if(subcategory!=""){
            List<Double> embeddingsFromSubcategory = getEmbeddingsFromSubcategory(subcategory);
            String mostSimilarQuestion = findMostSimilarQuestion(enrichedQuery, embeddedQuestion,embeddingsFromSubcategory, 0.7);
            return mostSimilarQuestion;
         }
         else{}///else выведи, что ничего найдено не было и тд

}





}
///else выведи, что ничего найдено не было и тд

}

 */

}