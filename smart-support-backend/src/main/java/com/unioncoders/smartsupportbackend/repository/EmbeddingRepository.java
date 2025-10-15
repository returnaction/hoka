package com.unioncoders.smartsupportbackend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmbeddingRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmbeddingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveEmbedding(
            String category,
            String subcategory,
            String question,
            String answer,
            String priority,
            String audience,
            List<Double> embedding
    ) {
        String embeddingStr = "[" + String.join(",", embedding.stream()
                .map(d -> String.format(java.util.Locale.US, "%.10f", d)) // формат без E
                .toList()) + "]";

        String sql = """
        INSERT INTO embeddings
            (category, subcategory, question, answer, priority, audience, embedding)
        VALUES (?, ?, ?, ?, ?, ?, ?::vector)
    """;

        jdbcTemplate.update(sql,
                category,
                subcategory,
                question,
                answer,
                priority,
                audience,
                embeddingStr
        );
    }


}
