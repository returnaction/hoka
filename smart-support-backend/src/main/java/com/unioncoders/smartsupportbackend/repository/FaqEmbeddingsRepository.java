package com.unioncoders.smartsupportbackend.repository;

import com.unioncoders.smartsupportbackend.service.FaqImportService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FaqEmbeddingsRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public FaqEmbeddingsRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int batchInsert(List<FaqImportService.FaqRowReady> rows) {
        String sql = """
                INSERT INTO faq_embeddings (category, subcategory, question, priority, audience, answer, embedding)
                VALUES (:category, :subcategory, :question, :priority, :audience, :answer, CAST(:embedding AS vector))
                """;

        MapSqlParameterSource[] batch = rows.stream().map(r -> new MapSqlParameterSource()
                .addValue("category", r.category())
                .addValue("subcategory", r.subcategory())
                .addValue("question", r.question())
                .addValue("priority", r.priority())
                .addValue("audience", r.audience())
                .addValue("answer", r.answer())
                .addValue("embedding", r.embeddingLiteral())
        ).toArray(MapSqlParameterSource[]::new);

        int[] res = jdbc.batchUpdate(sql, batch);
        int sum = 0;
        for (int n : res) sum += n;
        return sum;
    }

    /// 2.3 Поиск top-K ближайших записей по косинусу (pgvector)
    public List<Map<String, Object>> searchByEmbedding(List<Double> embedding, int k) {
        String vecLiteral = toVectorLiteral(embedding);

        String sql = """
                SELECT id, category, subcategory, question, answer,
                       1 - (embedding <=> CAST(:vec AS vector)) AS score
                FROM faq_embeddings
                ORDER BY embedding <=> CAST(:vec AS vector)
                LIMIT :k
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("vec", vecLiteral)
                .addValue("k", k);

        RowMapper<Map<String, Object>> rm = (rs, rowNum) -> Map.of(
                "id", rs.getLong("id"),
                "category", rs.getString("category"),
                "subcategory", rs.getString("subcategory"),
                "question", rs.getString("question"),
                "answer", rs.getString("answer"),
                "score", rs.getDouble("score")
        );

        return jdbc.query(sql, params, rm);
    }

    private static String toVectorLiteral(List<Double> v) {
        return v.stream()
                .map(d -> String.format(Locale.US, "%.10f", d))
                .collect(Collectors.joining(",", "[", "]"));
    }
    /*
    *
    *
    *
    *
    *
  //извлечение вопросов для категории (debug)
public Map<String, List<String>> getQuestionsGroupedByCategory() {
    String sql = "SELECT category, question FROM faq_embeddings";

    return jdbc.query(sql, rs -> {
        Map<String, List<String>> result = new HashMap<>();
        while (rs.next()) {
            String category = rs.getString("category");
            String question = rs.getString("question");
            result.computeIfAbsent(category, k -> new ArrayList<>()).add(question);
        }
        return result;
    });
}

//извлечение векторов для категории
public Map<String, List<List<Double>>> getEmbeddingsGroupedByCategory() {
    String sql = "SELECT category, embedding FROM faq_embeddings";

    return jdbc.query(sql, rs -> {
        Map<String, List<List<Double>>> result = new HashMap<>();
        while (rs.next()) {
            String category = rs.getString("category");
            String embeddingStr = rs.getString("embedding"); // формат: [0.123, -0.456, ...]

            List<Double> embedding = parseVector(embeddingStr);
            result.computeIfAbsent(category, k -> new ArrayList<>()).add(embedding);
        }
        return result;
    });
}

private static String toVectorLiteral(List<Double> v) {
    return v.stream() .map(d -> String.format(Locale.US, "%.10f", d)) .collect(Collectors.joining(",", "[", "]"));
}

//извлечение векторов для субкатегории
public Map<String, List<List<Double>>> getEmbeddingsGroupedBySubcategory(String category) {
    String sql = "SELECT subcategory, embedding FROM faq_embeddings WHERE category = ?";

    return jdbc.query(sql, new Object[]{category}, rs -> {
        Map<String, List<List<Double>>> result = new HashMap<>();
        while (rs.next()) {
            String subcategory = rs.getString("subcategory");
            String embeddingStr = rs.getString("embedding"); // format: [0.123, -0.456, ...]

            List<Double> embedding = parseVector(embeddingStr);
            result.computeIfAbsent(subcategory, k -> new ArrayList<>()).add(embedding);
        }
        return result;
    });
}

*
* //извлечение векторов из субкатегории
public List<Double> getEmbeddingsFromSubcategory(String subcategory) {
    String sql = "SELECT embedding FROM faq_embeddings WHERE subcategory = ?";

    return jdbc.query(sql, new Object[]{subcategory}, rs -> {
        Map<String, List<List<Double>>> result = new HashMap<>();
        while (rs.next()) {
            String embeddingStr = rs.getString("embedding"); // format: [0.123, -0.456, ...]

            List<Double> embedding = parseVector(embeddingStr);
            result.computeIfAbsent(subcategory, k -> new ArrayList<>()).add(embedding);
        }
        return result;
    });
}

    * */
}