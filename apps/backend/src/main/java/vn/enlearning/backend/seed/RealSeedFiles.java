package vn.enlearning.backend.seed;

import java.util.List;

import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.seed.SeedFiles.SeedPrompt;
import vn.enlearning.backend.seed.SeedFiles.SeedQuestion;

/**
 * Hình dạng của các file JSON dùng cho seeder dữ liệu thật (đợt 13, sinh bởi {@code tools/crawler}).
 * {@code topics.json}/{@code words.json}/{@code listening.json} nằm ở {@code resources/seed/real} (chủ đề,
 * từ vựng phẳng theo {@code topicSlug} chưa gom sẵn theo bộ thẻ, 8 bài nghe VOA thật). {@code reading.json}/
 * {@code writing.json}/{@code speaking.json} nằm ở {@code resources/seed} (bài seed cũ, thu gọn, được
 * {@code tools/crawler/src/annotateDemoImages.js} gắn thêm ảnh minh hoạ, đợt 13.7). Khác {@link SeedFiles}
 * ở chỗ mỗi bản ghi có thêm ảnh (Unsplash hoặc Pexels/Openverse/Wikimedia).
 */
final class RealSeedFiles {

	private RealSeedFiles() {
	}

	record RealTopic(String slug, String nameVi, String nameEn, String imageUrl, String imageAuthor,
			String imageAuthorUrl) {
	}

	record RealWord(String word, String phonetic, String partOfSpeechEn, String partOfSpeechVi, String meaningEn,
			String meaningVi, String example, String exampleMeaning, String topicSlug, Level level, String imageUrl,
			String imageAuthor, String imageAuthorUrl) {
	}

	record RealListening(ContentStatus status, String topic, Level level, String titleVi, String titleEn,
			String descriptionVi, String descriptionEn, String transcript, List<SeedQuestion> questions,
			String imageUrl, String imageAuthor, String imageAuthorUrl,
			String audioUrl, String audioPublicId, Integer durationSeconds,
			/** Ten cac tac gia Tatoeba doc audio (CC BY), chi de doi chieu, khong luu vao DB. */
			String audioCredit) {
	}

	record RealReading(ContentStatus status, String topic, Level level, int timeLimitMinutes, String titleVi,
			String titleEn, String descriptionVi, String descriptionEn, List<String> paragraphs,
			List<SeedQuestion> questions, String imageUrl, String imageAuthor, String imageAuthorUrl) {
	}

	record RealWriting(ContentStatus status, String topic, Level level, int suggestedMinutes, int minWords,
			String titleVi, String titleEn, String instructions, List<String> hints, String imageUrl,
			String imageAuthor, String imageAuthorUrl) {
	}

	record RealSpeaking(ContentStatus status, String topic, Level level, String titleVi, String titleEn,
			String descriptionVi, String descriptionEn, List<SeedPrompt> prompts, String imageUrl,
			String imageAuthor, String imageAuthorUrl) {
	}
}
