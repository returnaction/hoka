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

    // TODO: добавить индексы в БД

    private static final int BATCH_SIZE = 64;

    // колонки в Excel
    private static final int COL_CATEGORY    = 0;
    private static final int COL_SUBCATEGORY = 1;
    private static final int COL_QUESTION    = 2;
    private static final int COL_PRIORITY    = 3;
    private static final int COL_AUDIENCE    = 4;
    private static final int COL_ANSWER      = 5;

    private final SciboxClient sciboxClient;
    private final FaqEmbeddingsRepository faqEmbeddingsRepository;
    private final DataFormatter formatter = new DataFormatter(Locale.getDefault());

    public FaqImportService(SciboxClient sciboxClient, FaqEmbeddingsRepository faqEmbeddingsRepository) {
        this.sciboxClient = sciboxClient;
        this.faqEmbeddingsRepository = faqEmbeddingsRepository;
    }

    @Transactional
    public int importFromExcel(MultipartFile file) {
        List<FaqRow> buffer = new ArrayList<>(BATCH_SIZE);
        int insertedTotal = 0;

        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("В Excel нет листов)");
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // пропускаем заголовок

                String category    = getTrimmed(row.getCell(COL_CATEGORY));
                String subcategory = getTrimmed(row.getCell(COL_SUBCATEGORY));
                String question    = getTrimmed(row.getCell(COL_QUESTION));
                String priority    = getTrimmed(row.getCell(COL_PRIORITY));
                String audience    = getTrimmed(row.getCell(COL_AUDIENCE));
                String answer      = getTrimmed(row.getCell(COL_ANSWER));

                // обязательные поля
                if (isBlank(category) || isBlank(subcategory) || isBlank(question) || isBlank(answer)) {
                    continue;
                }

                String textForEmbedding = buildEmbeddingText(subcategory, question, answer);
                buffer.add(new FaqRow(category, subcategory, question, priority, audience, answer, textForEmbedding));

                if (buffer.size() >= BATCH_SIZE) {
                    insertedTotal += flushBatch(buffer);
                }
            }

            if (!buffer.isEmpty()) {
                insertedTotal += flushBatch(buffer);
            }

            return insertedTotal;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка импорта Excel: " + e.getMessage(), e);
        }
    }


    private int flushBatch(List<FaqRow> batch) {
        // 1 получаем эмбеддинги пачкой
        List<String> inputs = batch.stream().map(FaqRow::textForEmbedding).collect(Collectors.toList());
        List<List<Double>> vectors = sciboxClient.getEmbeddings(inputs);

        if (vectors.size() != batch.size()) {
            throw new IllegalStateException("Размер векторов (" + vectors.size() + ") != размеру батча (" + batch.size() + ")");
        }

        // 2 готовим DTO для репозитория
        List<FaqRowReady> ready = new ArrayList<>(batch.size());
        for (int i = 0; i < batch.size(); i++) {
            ready.add(batch.get(i).withEmbeddingLiteral(toVectorLiteral(vectors.get(i))));
        }

        // 3 вставляем в БД пачкой
        int inserted = faqEmbeddingsRepository.batchInsert(ready);

        batch.clear();
        return inserted;
    }


    private static String buildEmbeddingText(String subcategory, String question, String answer) {
        String a = answer == null ? "" : answer.trim();
        if (a.length() > 200) a = a.substring(0, 200);
        return subcategory + " | " + question + " | " + a;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


    private static String toVectorLiteral(List<Double> v) {
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (Double d : v) j.add(String.format(Locale.US, "%.10f", d));
        return j.toString();
    }


    private String getTrimmed(Cell cell) {
        if (cell == null) return null;
        String s = formatter.formatCellValue(cell);
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
    ) {}
}