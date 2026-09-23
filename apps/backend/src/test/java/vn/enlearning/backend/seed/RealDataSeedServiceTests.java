package vn.enlearning.backend.seed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.enums.QuestionKind;

/**
 * Nạp dữ liệu thật đợt 13 ({@code seed/real/*.json} và {@code seed/{reading,writing,speaking}.json}, xem
 * {@link RealDataSeedService}) trong giao dịch test rồi rollback. Không phụ thuộc DB rỗng: seeder này cộng
 * thêm dữ liệu và tự bỏ qua bản ghi đã có (idempotent), nên chạy lại lần hai trong cùng giao dịch phải không
 * tạo thêm gì.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RealDataSeedServiceTests {

	@Autowired
	private RealDataSeedService realDataSeedService;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private FlashcardDeckRepository decks;
	@Autowired
	private FlashcardRepository flashcards;
	@Autowired
	private ListeningLessonRepository listeningLessons;
	@Autowired
	private ReadingLessonRepository readingLessons;
	@Autowired
	private WritingPromptRepository writingPrompts;
	@Autowired
	private SpeakingLessonRepository speakingLessons;
	@Autowired
	private QuestionRepository questions;

	@Test
	@DisplayName("Nạp dữ liệu thật đúng 20 chủ đề, có từ/bài học, và chạy lần hai thì bỏ qua toàn bộ")
	void seedsRealDataOnceAndOnlyOnce() {
		RealDataSeedService.Report first = realDataSeedService.seed();

		assertThat(first.topicsCreated() + first.topicsSkipped()).isEqualTo(20);
		assertThat(first.wordsCreated()).isPositive();
		assertThat(first.listeningCreated() + first.listeningSkipped()).isPositive();
		assertThat(first.readingCreated() + first.readingSkipped()).isPositive();
		assertThat(first.writingCreated() + first.writingSkipped()).isPositive();
		assertThat(first.speakingCreated() + first.speakingSkipped()).isPositive();

		assertThat(flashcards.count()).isGreaterThanOrEqualTo(first.wordsCreated());
		assertThat(listeningLessons.count()).isGreaterThanOrEqualTo(first.listeningCreated());
		assertThat(readingLessons.count()).isGreaterThanOrEqualTo(first.readingCreated());
		assertThat(writingPrompts.count()).isGreaterThanOrEqualTo(first.writingCreated());
		assertThat(speakingLessons.count()).isGreaterThanOrEqualTo(first.speakingCreated());

		long topicsBefore = topics.count();
		long decksBefore = decks.count();
		long flashcardsBefore = flashcards.count();
		long listeningBefore = listeningLessons.count();
		long readingBefore = readingLessons.count();
		long writingBefore = writingPrompts.count();
		long speakingBefore = speakingLessons.count();

		RealDataSeedService.Report second = realDataSeedService.seed();

		assertThat(second.topicsCreated()).isZero();
		assertThat(second.wordsCreated()).isZero();
		assertThat(second.listeningCreated()).isZero();
		assertThat(second.readingCreated()).isZero();
		assertThat(second.writingCreated()).isZero();
		assertThat(second.speakingCreated()).isZero();
		assertThat(topics.count()).isEqualTo(topicsBefore);
		assertThat(decks.count()).isEqualTo(decksBefore);
		assertThat(flashcards.count()).isEqualTo(flashcardsBefore);
		assertThat(listeningLessons.count()).isEqualTo(listeningBefore);
		assertThat(readingLessons.count()).isEqualTo(readingBefore);
		assertThat(writingPrompts.count()).isEqualTo(writingBefore);
		assertThat(speakingLessons.count()).isEqualTo(speakingBefore);
	}

	@Test
	@DisplayName("Mỗi bộ thẻ thật gắn đúng chủ đề, mỗi câu trắc nghiệm có đúng một đáp án đúng")
	void seededContentIsWellFormed() {
		realDataSeedService.seed();

		for (var deck : decks.findAll()) {
			Topic topic = deck.getTopic();
			assertThat(topic).isNotNull();
			assertThat(deck.getTitleVi()).contains(topic.getNameVi());
		}

		for (Question question : questions.findAll()) {
			int owners = (question.getListeningLesson() != null ? 1 : 0) + (question.getExam() != null ? 1 : 0)
					+ (question.getReadingLesson() != null ? 1 : 0);
			assertThat(owners).as("chủ sở hữu của câu %s", question.getContent()).isEqualTo(1);
			if (question.getKind() == QuestionKind.SINGLE_CHOICE) {
				assertThat(question.getOptions()).hasSizeGreaterThanOrEqualTo(2);
				assertThat(question.getOptions().stream().filter(option -> option.isCorrect()).count()).isEqualTo(1);
			}
		}
	}
}
