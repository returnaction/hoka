package com.unioncoders.smartsupportbackend.service;

import com.unioncoders.smartsupportbackend.repository.FaqEmbeddingsRepository;
import com.unioncoders.smartsupportbackend.service.client.SciboxClient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FaqImportService {

    /*
Прочитать Excel (по заголовкам, не по позициям столбцов).
Нормализовать строки: trim(), схлопнуть двойные пробелы, при желании ё→е.
Сформировать батч текстов для эмбеддинга (лучше по 64–128 штук):
достаточно эмбеддить question (быстро и работает),
продвинутый вариант: subcategory + " | " + question + " | " + answer.substring(0,200).
Вызвать SciBox /v1/embeddings (bge-m3) на весь батч → получить vector(1024).
Вставить строки в БД (batch insert).
транзакция на батч (например, по 500–1000 строк),
пропускать пустые/битые строки,
(опц.) защита от дублей: уникальный ключ на (subcategory, question) и ON CONFLICT DO NOTHING.
По окончании — создать/обновить ivfflat индекс и ANALYZE (если загружали «с нуля»).
//TODO доавбить индексы спросить у Vlada на что будем добавлять
     */

    private static final int BATCH_SIZE = 64;

    private final SciboxClient sciboxClient;
    private final FaqEmbeddingsRepository faqEmbeddingsRepository;

    public FaqImportService(SciboxClient sciboxClient, FaqEmbeddingsRepository faqEmbeddingsRepository) {
        this.sciboxClient = sciboxClient;
        this.faqEmbeddingsRepository = faqEmbeddingsRepository;
    }

    /// 1.2
    @Transactional
    public int importFromExcel(MultipartFile file) {
        List<FaqRow> buffer = new ArrayList<>();
        int insertedTotal = 0;

        try (InputStream is = file.getInputStream();
             Workbook wb = new XSSFWorkbook(is)) {

            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // пропустить заголовок

                String category = getString(row.getCell(0));
                String subcategory = getString(row.getCell(1));
                String question = getString(row.getCell(2));
                String priority = getString(row.getCell(3));
                String audience = getString(row.getCell(4));
                String answer = getString(row.getCell(5));

                if (isBlank(category) || isBlank(subcategory) || isBlank(question) || isBlank(answer)) {
                    continue;
                }

                String textForEmbedding = buildEmbeddingText(subcategory, question, answer);
                buffer.add(new FaqRow(category, subcategory, question, priority, audience, answer, textForEmbedding));

                if (buffer.size() >= BATCH_SIZE) {
                    insertedTotal += flushBatch(buffer);
                }
            }
            // хвост
            if (!buffer.isEmpty()) {
                insertedTotal += flushBatch(buffer);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка импорта Excel: " + e.getMessage(), e);
        }

        return insertedTotal;
    }

    /// 1.3
    private int flushBatch(List<FaqRow> batch) {
        // 1) эмбеддим пачкой
        List<String> inputs = batch.stream().map(FaqRow::textForEmbedding).collect(Collectors.toList());
        List<List<Double>> vectors = sciboxClient.getEmbeddings(inputs);

        // 2) соберём строки для вставки
        List<FaqRowReady> ready = new ArrayList<>(batch.size());
        for (int i = 0; i < batch.size(); i++) {
            ready.add(batch.get(i).withEmbeddingLiteral(toVectorLiteral(vectors.get(i))));
        }

        // 3) пишем в БД пачкой
        int inserted = faqEmbeddingsRepository.batchInsert(ready);

        batch.clear();
        return inserted;
    }

    private static String buildEmbeddingText(String subcategory, String question, String answer) {
        String a = answer == null ? "" : answer;
        if (a.length() > 200) a = a.substring(0, 200);
        return subcategory + " | " + question + " | " + a;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String toVectorLiteral(List<Double> v) {
        // pgvector принимает текстовый литерал: '[0.123, -0.456, ...]'
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (Double d : v) j.add(String.format(Locale.US, "%.10f", d));
        return j.toString();
    }

    private static String getString(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        String s = cell.getStringCellValue();
        return s != null ? s.trim() : null;
    }

    //  DTO для вставки
    public record FaqRow(
            String category, String subcategory, String question,
            String priority, String audience, String answer,
            String textForEmbedding
    ) {
        public FaqRowReady withEmbeddingLiteral(String embeddingLiteral) {
            return new FaqRowReady(category, subcategory, question, priority, audience, answer, embeddingLiteral);
        }
    }

    public record FaqRowReady(
            String category, String subcategory, String question,
            String priority, String audience, String answer,
            String embeddingLiteral
    ) {
    }
}
