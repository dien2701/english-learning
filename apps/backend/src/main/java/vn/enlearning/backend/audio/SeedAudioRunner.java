package vn.enlearning.backend.audio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.audio.dto.ListeningAudioResponse;
import vn.enlearning.backend.audio.service.ListeningAudioService;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.entity.ListeningLesson;

/**
 * Lệnh chạy một lần: bật bằng {@code app.audio.generate-seed=true} (env {@code AUDIO_GENERATE_SEED=true}). Sau khi seed
 * xong, sinh audio cho mọi bài nghe chưa có {@code audio_url} (bài đã có thì bỏ qua nên chạy lại vô hại), rồi ghi URL
 * Cloudinary vào {@code seed-demo/listening.json} để lần seed sau không phải sinh lại. URL cục bộ ({@code /api/media/...})
 * không ghi vào JSON vì file nằm ngoài git. Xong thì đặt lại cờ về false.
 */
@Slf4j
@Component
@Order(100) // sau DevSeedRunner
@ConditionalOnProperty(prefix = "app.audio", name = "generate-seed", havingValue = "true")
@RequiredArgsConstructor
public class SeedAudioRunner implements ApplicationRunner {

	/** Đường dẫn tương đối theo thư mục chạy backend ({@code apps/backend}). */
	static final Path SEED_FILE = Path.of("src/main/resources/seed-demo/listening.json");

	private final ListeningLessonRepository lessons;
	private final ListeningAudioService audio;

	@Override
	public void run(ApplicationArguments args) {
		List<ListeningLesson> pending = lessons.findAllWithoutAudio();
		log.info("Sinh audio seed: {} bài nghe chưa có audio.", pending.size());
		Map<String, String> generated = new LinkedHashMap<>();
		int done = 0;
		for (ListeningLesson lesson : pending) {
			String title = lesson.getTitleEn();
			log.info("Sinh audio seed [{}/{}]: {}", ++done, pending.size(), title);
			try {
				ListeningAudioResponse result = audio.generate(lesson.getId());
				generated.put(title, result.audioUrl());
			} catch (RuntimeException e) {
				log.warn("Bỏ qua bài '{}': {}", title, e.getMessage());
			}
		}
		log.info("Sinh audio seed xong: {}/{} bài thành công.", generated.size(), pending.size());
		writeBack(generated);
		log.info("Hãy đặt lại AUDIO_GENERATE_SEED=false (hoặc bỏ cờ) để lần khởi động sau không chạy lại.");
	}

	private static void writeBack(Map<String, String> generated) {
		Map<String, String> remote = new LinkedHashMap<>();
		generated.forEach((title, url) -> {
			if (url != null && url.startsWith("http")) {
				remote.put(title, url);
			}
		});
		if (remote.isEmpty()) {
			log.info("Không có URL Cloudinary để ghi vào seed JSON (audio cục bộ không ghi ngược).");
			return;
		}
		if (!Files.isRegularFile(SEED_FILE)) {
			log.warn("Không thấy {}: hãy chạy backend từ apps/backend để ghi URL vào seed JSON.", SEED_FILE);
			return;
		}
		try {
			String json = Files.readString(SEED_FILE, StandardCharsets.UTF_8);
			int written = 0;
			for (Map.Entry<String, String> e : remote.entrySet()) {
				if (e.getKey().contains("\"") || e.getKey().contains("\\")) {
					log.warn("Không ghi ngược được bài '{}' (tiêu đề có ký tự cần escape).", e.getKey());
					continue;
				}
				// Chỉ thay "audioUrl": null đầu tiên sau titleEn của chính bài đó.
				Pattern p = Pattern.compile(
						"(\"titleEn\":\\s*\"" + Pattern.quote(e.getKey()) + "\"(?:(?!\"audioUrl\")(?s:.))*?\"audioUrl\":\\s*)null");
				Matcher m = p.matcher(json);
				if (m.find()) {
					json = m.replaceFirst(Matcher.quoteReplacement(m.group(1) + "\"" + e.getValue() + "\""));
					written++;
				}
			}
			Files.writeString(SEED_FILE, json, StandardCharsets.UTF_8);
			log.info("Đã ghi {} URL audio vào {}.", written, SEED_FILE);
		} catch (IOException ex) {
			log.warn("Không ghi được {}: {}", SEED_FILE, ex.getMessage());
		}
	}
}
