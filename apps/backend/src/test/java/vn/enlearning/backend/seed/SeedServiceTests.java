package vn.enlearning.backend.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Role;

/**
 * Nạp toàn bộ dữ liệu mẫu trong giao dịch test rồi rollback. Bỏ qua khi DB dev đã có người dùng
 * (ví dụ đã chạy profile dev), vì khi đó {@code seedIfEmpty} đúng ra phải từ chối nạp.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeedServiceTests {

	@Autowired
	private SeedService seedService;
	@Autowired
	private UserRepository users;
	@Autowired
	private UserSettingRepository settings;
	@Autowired
	private PasswordEncoder passwordEncoder;
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
	private SpeakingLessonRepository speakingLessons;
	@Autowired
	private WritingPromptRepository writingPrompts;
	@Autowired
	private ExamRepository exams;
	@Autowired
	private QuestionRepository questions;

	@Test
	@DisplayName("Nạp đủ số dòng như file JSON (khớp số liệu xuất từ mock), và chạy lần hai thì bỏ qua")
	void seedsEverythingOnceAndOnlyOnce() {
		assumeTrue(users.countIncludingDeleted() == 0, "DB dev đã có người dùng");

		SeedService.Report report = seedService.seedIfEmpty().orElseThrow();

		assertThat(report).isEqualTo(new SeedService.Report(3, 6, 6, 36, 4, 4, 3, 5, 3, 45, 144));
		assertThat(users.count()).isEqualTo(3);
		assertThat(settings.count()).isEqualTo(3);
		assertThat(topics.count()).isEqualTo(6);
		assertThat(decks.count()).isEqualTo(6);
		assertThat(flashcards.count()).isEqualTo(36);
		assertThat(listeningLessons.count()).isEqualTo(4);
		assertThat(readingLessons.count()).isEqualTo(4);
		assertThat(speakingLessons.count()).isEqualTo(3);
		assertThat(writingPrompts.count()).isEqualTo(5);
		assertThat(exams.count()).isEqualTo(3);
		assertThat(questions.count()).isEqualTo(45);

		assertThat(seedService.seedIfEmpty()).isEmpty();
		assertThat(users.count()).isEqualTo(3);
	}

	@Test
	@DisplayName("Ba tài khoản mẫu: đúng vai trò và trạng thái, mật khẩu 123456 được băm BCrypt cost 12")
	void seedsTheThreeDemoAccounts() {
		assumeTrue(users.countIncludingDeleted() == 0, "DB dev đã có người dùng");
		seedService.seedIfEmpty();

		User student = users.findByEmail("hocvien@enlearning.vn").orElseThrow();
		User admin = users.findByEmail("admin@enlearning.vn").orElseThrow();
		User locked = users.findByEmail("khoa@enlearning.vn").orElseThrow();

		assertThat(student.getRole()).isEqualTo(Role.USER);
		assertThat(student.getStatus()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
		assertThat(locked.getStatus()).isEqualTo(AccountStatus.LOCKED);
		for (User user : new User[] { student, admin, locked }) {
			assertThat(passwordEncoder.matches("123456", user.getPasswordHash())).isTrue();
			assertThat(user.getPasswordHash()).contains("$12$");
			assertThat(settings.findByUserId(user.getId())).isPresent();
		}
	}

	@Test
	@DisplayName("Nội dung nạp đúng: cột JSON, câu hỏi thuộc đúng một chủ sở hữu, mỗi câu trắc nghiệm có đúng một đáp án đúng")
	void seededContentIsWellFormed() {
		assumeTrue(users.countIncludingDeleted() == 0, "DB dev đã có người dùng");
		seedService.seedIfEmpty();

		ReadingLesson reading = readingLessons.findAll().getFirst();
		assertThat(reading.getParagraphs()).isNotEmpty();
		assertThat(reading.getWordCount()).isEqualTo(reading.getParagraphs().stream()
				.mapToInt(paragraph -> paragraph.trim().split("\\s+").length).sum());
		assertThat(reading.getCreatedBy().getRole()).isEqualTo(Role.ADMIN);

		SpeakingLesson speaking = speakingLessons.findAll().getFirst();
		assertThat(speaking.getPrompts()).isNotEmpty().allSatisfy(prompt -> {
			assertThat(prompt.id()).isNotNull();
			assertThat(prompt.text()).isNotBlank();
		});

		for (Question question : questions.findAll()) {
			int owners = (question.getListeningLesson() != null ? 1 : 0)
					+ (question.getReadingLesson() != null ? 1 : 0)
					+ (question.getExam() != null ? 1 : 0);
			assertThat(owners).as("chủ sở hữu của câu %s", question.getContent()).isEqualTo(1);
			if (question.getKind() == QuestionKind.SINGLE_CHOICE) {
				assertThat(question.getOptions()).hasSizeGreaterThanOrEqualTo(2);
				assertThat(question.getOptions().stream().filter(option -> option.isCorrect()).count()).isEqualTo(1);
				assertThat(question.getAcceptedAnswers()).isEmpty();
			} else {
				assertThat(question.getOptions()).isEmpty();
				assertThat(question.getAcceptedAnswers()).isNotEmpty();
			}
		}
	}
}
