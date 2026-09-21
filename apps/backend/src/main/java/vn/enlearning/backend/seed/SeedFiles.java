package vn.enlearning.backend.seed;

import java.util.List;

import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.entity.enums.Skill;

/**
 * Hình dạng của các file JSON trong {@code resources/seed}. Dữ liệu được chuyển một lần từ mock của
 * frontend ({@code src/mocks/data}); từ nay JSON là nguồn sự thật. Chủ đề được tham chiếu theo {@code slug}.
 */
final class SeedFiles {

	private SeedFiles() {
	}

	record SeedUser(String email, String fullName, Role role, AccountStatus status, String password) {
	}

	record SeedTopic(String slug, String nameVi, String nameEn) {
	}

	record SeedCard(String word, String phonetic, String meaningVi, String meaningEn, String partOfSpeechVi,
			String partOfSpeechEn, String example, String exampleMeaning, String imageUrl, String audioUrl) {
	}

	record SeedDeck(String topic, String titleVi, String titleEn, String descriptionVi, String descriptionEn,
			String coverImageUrl, Level level, List<SeedCard> cards) {
	}

	record SeedOption(String content, boolean correct) {
	}

	/** Câu trắc nghiệm có {@code options}; câu điền từ có {@code acceptedAnswers}. */
	record SeedQuestion(QuestionKind kind, Skill skill, String content, String explanation,
			List<SeedOption> options, List<String> acceptedAnswers) {
	}

	record SeedListening(String topic, String titleVi, String titleEn, String descriptionVi, String descriptionEn,
			Level level, String audioUrl, int durationSeconds, String transcript, List<SeedQuestion> questions) {
	}

	record SeedReading(String topic, String titleVi, String titleEn, String descriptionVi, String descriptionEn,
			Level level, int timeLimitMinutes, List<String> paragraphs, List<SeedQuestion> questions) {
	}

	record SeedPrompt(String text, String phonetic, String meaningVi) {
	}

	record SeedSpeaking(String topic, String titleVi, String titleEn, String descriptionVi, String descriptionEn,
			Level level, List<SeedPrompt> prompts) {
	}

	record SeedWriting(String topic, String titleVi, String titleEn, Level level, int suggestedMinutes,
			int minWords, String instructions, List<String> hints) {
	}

	record SeedExam(String titleVi, String titleEn, String descriptionVi, String descriptionEn, Level level,
			int timeLimitMinutes, List<SeedQuestion> questions) {
	}
}
