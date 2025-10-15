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
}
