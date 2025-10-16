package com.unioncoders.smartsupportbackend.service.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unioncoders.smartsupportbackend.config.SciboxProperties;
import com.unioncoders.smartsupportbackend.model.SciboxResponse;
import com.unioncoders.smartsupportbackend.util.TaxonomyProvider;
import com.unioncoders.smartsupportbackend.repository.FaqEmbeddingsRepository;
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
    private final FaqEmbeddingsRepository faqEmbeddingsRepository;

    public SciboxClient(
            RestClient sciboxRestClient,
            SciboxProperties props,
            TaxonomyProvider taxonomyProvider,
            FaqEmbeddingsRepository faqEmbeddingsRepository  // добавляем сюда
    ) {
        this.client = sciboxRestClient;
        this.props = props;
        this.taxonomyProvider = taxonomyProvider;
        this.faqEmbeddingsRepository = faqEmbeddingsRepository; // инициализация final
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
                "Ты — помощник по нормализации текста клиентских сообщений.",
                "Твоя задача — переписать текст так, чтобы он был нейтральным и понятным.",
                "Правила переписывания:",
                "- Убери ненормативную лексику и жаргон.",
                "- Не изменяй смысл текста.",
                "- Не заменяй названия продуктов, объектов или любых слов на похожие банковские термины.",
                "- Не добавляй информацию, которой нет в исходном тексте.",
                "- Если текст уже нейтральный — верни его без изменений.",
                "- Перепиши строго в виде запроса, без обращения к клиенту, без извинений, без вопросов от лица поддержки.",
                "Пример: \"что с моей картошкой?\" → \"что с моей картошкой?\""
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
                "Ты — помощник, извлекающий ключевые сущности из текста клиента банка.",
                "Сущность — это важный объект запроса (продукт, услуга, вклад, карта и т.д.).",
                "Верни строго JSON-массив строк БЕЗ дополнительного текста.",
                "Пример: [\"СуперСемь\"]"
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

    private String resolveCategoryWithEmbeddings(
            String enrichedQuestion,
            List<Double> embeddedQuestion,
            Map<String, List<List<Double>>> embeddingsByCategory,
            double threshold,
            int retryCount
    ) {
        final int MAX_RETRIES = 3;
        final double MIN_SIMILARITY = 0.5;        // минимальное абсолютное сходство
        final double SIMILARITY_DIFF_THRESHOLD = 0.03; // минимальная разница между топ-2

        Map<String, Double> similarities = computeCategorySimilarities(embeddedQuestion, embeddingsByCategory);
        if (similarities.isEmpty()) return "";

        // Сортируем по убыванию сходства
        List<Map.Entry<String, Double>> sorted = similarities.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .toList();

        Map.Entry<String, Double> top1 = sorted.get(0);
        Map.Entry<String, Double> top2 = sorted.size() > 1 ? sorted.get(1) : null;

        // Проверяем абсолютное сходство
        if (top1.getValue() < MIN_SIMILARITY) {
            return ""; // нет уверенного совпадения → пустая категория
        }

        // Проверяем разницу между топ-2
        double diff = (top2 != null) ? Math.abs(top1.getValue() - top2.getValue()) : 1.0;
        if (diff >= SIMILARITY_DIFF_THRESHOLD) {
            return top1.getKey();
        }

        // Если близко по сходству — пробуем LLM
        String category = resolveWithLLM(enrichedQuestion, top1.getKey(), top2 != null ? top2.getKey() : null);

        if ((category == null || category.isBlank()) && retryCount < MAX_RETRIES) {
            // переформулируем и пробуем снова
            String rephrasedQuestion = changeQuestionToSimilarText(enrichedQuestion);
            List<String> newEntities = retrieveEntities(rephrasedQuestion);
            String rephrasedEnrichedQuery = enrichQuery(rephrasedQuestion, newEntities);
            List<Double> newEmbeddedQuestion = getEmbedding(rephrasedEnrichedQuery);

            return resolveCategoryWithEmbeddings(
                    rephrasedEnrichedQuery,
                    newEmbeddedQuestion,
                    embeddingsByCategory,
                    threshold,
                    retryCount + 1
            );
        }

        return category != null ? category : "";
    }


    /** Считает косинусные сходства категорий */
    private Map<String, Double> computeCategorySimilarities(
            List<Double> embeddedQuestion,
            Map<String, List<List<Double>>> embeddingsByCategory
    ) {
        Map<String, Double> similarities = new HashMap<>();
        for (Map.Entry<String, List<List<Double>>> entry : embeddingsByCategory.entrySet()) {
            String category = entry.getKey();
            List<List<Double>> vectors = entry.getValue();

            // Берём максимальное сходство с любым вопросом в категории
            double maxSim = vectors.stream()
                    .mapToDouble(v -> SciboxClient.cosineSimilarity(embeddedQuestion, v))
                    .max()
                    .orElse(0.0);

            similarities.put(category, maxSim);
        }
        return similarities;
    }


    /** Если категории слишком близкие — запрос к LLM */
    private String resolveWithLLM(String enrichedQuestion, String cat1, String cat2) {
        String systemPrompt = String.format("""
        Тебе нужно отнести данный вопрос: "%s"
        к одной из данных категорий: %s, %s.
        Формат ответа: категория, score
        """, enrichedQuestion, cat1, cat2);

        Map<String, Object> body = Map.of(
                "model", "Qwen2.5-72B-Instruct-AWQ",
                "messages", List.of(Map.of("role", "user", "content", systemPrompt)),
                "temperature", 0.3,
                "max_tokens", 100
        );

        try {
            Map<String, Object> res = postJson("/chat/completions", body);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");
            if (choices == null || choices.isEmpty()) return "";

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = ((String) message.get("content")).trim();

            String[] parts = content.split(",");
            if (parts.length == 0) return "";

            return parts[0].trim(); // просто возвращаем категорию
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /** При низкой уверенности — перефразируем вопрос и пробуем снова */
    private String handleLowConfidence(
            String enrichedQuestion,
            List<Double> embeddedQuestion,
            Map<String, List<List<Double>>> embeddingsByCategory,
            double threshold,
            int retryCount
    ) {
        String rephrasedQuestion = changeQuestionToSimilarText(enrichedQuestion);
        List<String> newEntities = retrieveEntities(rephrasedQuestion);
        String rephrasedEnrichedQuery = enrichQuery(rephrasedQuestion, newEntities);
        List<Double> newEmbeddedQuestion = getEmbedding(rephrasedEnrichedQuery);

        return resolveCategoryWithEmbeddings(
                rephrasedEnrichedQuery,
                newEmbeddedQuestion,
                embeddingsByCategory,
                threshold,
                retryCount + 1
        );
    }
    public String findMostSimilarQuestion(
            String enrichedQuestion,
            List<Double> embeddedQuestion,
            Map<String, List<Double>> questionEmbeddings,
            double threshold
    ) {
        Map<String, Double> similarities = computeQuestionSimilarities(embeddedQuestion, questionEmbeddings);
        if (similarities.isEmpty()) return "";

        List<Map.Entry<String, Double>> sorted = similarities.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .toList();

        Map.Entry<String, Double> top1 = sorted.get(0);
        double bestSim = top1.getValue();

        // если уверенно — вернуть лучший результат
        if (bestSim >= threshold) {
            return top1.getKey();
        }

        // иначе спросить LLM
        List<String> topQuestions = sorted.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        String resolved = resolveQuestionWithLLM(enrichedQuestion, topQuestions);
        if (resolved.isBlank()) {
            return ""; // ничего не понял — вернуть пустую строку
        }

        // если LLM выбрал что-то из топа — вернуть
        for (String q : topQuestions) {
            if (resolved.toLowerCase().contains(q.toLowerCase())) {
                return q;
            }
        }

        //  иначе — не найдено
        return "";
    }


    /** Считает косинусные сходства между вопросом и шаблонными вопросами */
    private Map<String, Double> computeQuestionSimilarities(
            List<Double> embeddedQuestion,
            Map<String, List<Double>> questionEmbeddings
    ) {
        Map<String, Double> similarities = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : questionEmbeddings.entrySet()) {
            double sim = cosineSimilarity(embeddedQuestion, entry.getValue());
            similarities.put(entry.getKey(), sim);
        }
        return similarities;
    }

    /** Вызывает LLM, чтобы выбрать наиболее похожий вопрос */
    private String resolveQuestionWithLLM(String enrichedQuestion, List<String> topQuestions) {
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
                    "messages", List.of(Map.of("role", "user", "content", systemPrompt)),
                    "temperature", 0.3,
                    "max_tokens", 128
            );

            Map<String, Object> res = postJson("/chat/completions", body);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) res.get("choices");
            if (choices == null || choices.isEmpty()) return "";

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return ((String) message.get("content")).trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    //ищем final question
    public String retrieveTheQuestion(String question) {
        // 1️⃣ Нормализуем и обогащаем
        String normalisedQuestion = normaliseText(question);
        List<String> entities = retrieveEntities(normalisedQuestion);
        String enrichedQuery = enrichQuery(normalisedQuestion, entities);
        List<Double> embeddedQuestion = getEmbedding(enrichedQuery);

        Map<String, List<List<Double>>> embeddingsByCategory = faqEmbeddingsRepository.getEmbeddingsGroupedByCategory();

        String category = resolveCategoryWithEmbeddings(
                enrichedQuery, embeddedQuestion, embeddingsByCategory, 0.7, 0
        );

        if (category == null || category.isBlank()) {
            System.out.println("⚠️ Не удалось определить категорию для запроса: " + question);
            return "";
        }

        Map<String, List<List<Double>>> embeddingsBySubcategory = faqEmbeddingsRepository.getEmbeddingsGroupedBySubcategory(category);

        String subcategory = resolveCategoryWithEmbeddings(
                enrichedQuery, embeddedQuestion, embeddingsBySubcategory, 0.7, 0
        );

        if (subcategory == null || subcategory.isBlank()) {
            System.out.println("⚠️ Категория найдена (" + category + "), но подкатегория не определена.");
            return "";
        }

        Map<String, List<Double>> questionEmbeddings = faqEmbeddingsRepository.getEmbeddingsFromSubcategory(subcategory);

        String mostSimilarQuestion = findMostSimilarQuestion(
                enrichedQuery, embeddedQuestion, questionEmbeddings, 0.7
        );

        if (mostSimilarQuestion == null || mostSimilarQuestion.isBlank()) {
            System.out.println("⚠️ Подкатегория найдена (" + subcategory + "), но похожий вопрос не обнаружен.");
            return "";
        }

        System.out.println("✅ Найден вопрос: " + mostSimilarQuestion);
        return mostSimilarQuestion;
    }

    //debug
    public String testRetrieveCategory(String question) {
        // 1️⃣ Нормализация
        String normalisedQuestion = normaliseText(question);
        System.out.println("Нормализованный вопрос: " + normalisedQuestion);

        // 2️⃣ Извлечение сущностей
        List<String> entities = retrieveEntities(normalisedQuestion);
        System.out.println("🔹 Сущности: " + entities);

        // 3️⃣ Обогащение запроса
        String enrichedQuery = enrichQuery(normalisedQuestion, entities);
        System.out.println("Обогащённый запрос: " + enrichedQuery);

        // 4️⃣ Эмбеддинг
        List<Double> embeddedQuestion = getEmbedding(enrichedQuery);

        // 5️⃣ Эмбеддинги по категориям
        Map<String, List<List<Double>>> embeddingsByCategory = faqEmbeddingsRepository.getEmbeddingsGroupedByCategory();

        // 6️⃣ Определяем категорию
        String category = resolveCategoryWithEmbeddings(enrichedQuery, embeddedQuestion, embeddingsByCategory, 0.7, 0);
        System.out.println("Решённая категория: " + category);

        return category;
    }


    public void testSubcategoryResolution(String category, String question) {

        System.out.println("\n🔹 Тест подкатегории для категории: " + category);
        System.out.println("Вопрос: " + question);

        // 1️⃣ Нормализуем вопрос
        String normalized = normaliseText(question);

        // 2️⃣ Извлекаем сущности
        List<String> entities = retrieveEntities(normalized);

        // 3️⃣ Обогащаем запрос
        String enriched = enrichQuery(normalized, entities);

        // 4️⃣ Получаем эмбеддинг вопроса
        List<Double> embedded = getEmbedding(enriched);

        // 5️⃣ Загружаем эмбеддинги подкатегорий в рамках категории
        Map<String, List<List<Double>>> embeddingsBySubcategory =
                faqEmbeddingsRepository.getEmbeddingsGroupedBySubcategory(category);

        if (embeddingsBySubcategory == null || embeddingsBySubcategory.isEmpty()) {
            System.out.println("⚠️ Для категории " + category + " нет подкатегорий в базе.");
            return;
        }

        // 6️⃣ Определяем подкатегорию
        String subcategory = resolveCategoryWithEmbeddings(
                enriched,
                embedded,
                embeddingsBySubcategory,
                0.7,  // порог уверенности
                0     // количество повторов
        );

        if (subcategory == null || subcategory.isBlank()) {
            System.out.println("❌ Подкатегория не определена.");
        } else {
            System.out.println("✅ Определена подкатегория: " + subcategory);
        }
    }



}