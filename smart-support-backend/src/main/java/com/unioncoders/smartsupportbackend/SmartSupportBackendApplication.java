package com.unioncoders.smartsupportbackend;

import com.unioncoders.smartsupportbackend.service.ExcelCategoryExtractor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartSupportBackendApplication implements CommandLineRunner {

	private final ExcelCategoryExtractor extractor;

	public SmartSupportBackendApplication(ExcelCategoryExtractor extractor) {
		this.extractor = extractor;
	}

	public static void main(String[] args) {
		SpringApplication.run(SmartSupportBackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var categories = extractor.extractCategories(
				"C:\\Users\\Oberg\\OneDrive\\Desktop\\hoka\\smart-support-backend\\src\\main\\resources\\data\\knowledge.xlsx"
		);

		categories.forEach((main, subs) -> {
			System.out.println("📂 " + main);
			subs.forEach(sub -> System.out.println("   └─ " + sub));
		});
	}
}

/**
 Клиент → Spring Boot (REST API)
 ↓
 SciBox (LLM / Embeddings API)
 ↓
 Pinecone (векторное хранилище)
 ↓
 БД шаблонов (локально / CSV)



 🚀 Что делаем прямо сейчас (пошагово)

 Ты стоишь между шагами 2 и 3 из этого плана 👇

 Шаг	Что делаем	Зачем
 ✅ 1	Отправляем текст в SciBox (уже готово)	Проверили интеграцию
 🟡 2	Добавляем список категорий и подкатегорий (в коде)	Чтобы модель знала, что можно выбирать
 🟢 3	Включаем эти категории в prompt	Чтобы классификация была точной
 */
