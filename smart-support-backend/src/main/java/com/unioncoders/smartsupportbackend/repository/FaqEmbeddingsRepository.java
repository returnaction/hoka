package com.unioncoders.smartsupportbackend.repository;

import com.unioncoders.smartsupportbackend.service.FaqImportService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
