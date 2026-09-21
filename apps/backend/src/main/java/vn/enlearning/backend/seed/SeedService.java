package vn.enlearning.backend.seed;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import vn.enlearning.backend.auth.dto.Emails;
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
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.Flashcard;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.QuestionOption;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.SpeakingPrompt;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.seed.SeedFiles.SeedCard;
import vn.enlearning.backend.seed.SeedFiles.SeedDeck;
import vn.enlearning.backend.seed.SeedFiles.SeedExam;
import vn.enlearning.backend.seed.SeedFiles.SeedListening;
import vn.enlearning.backend.seed.SeedFiles.SeedOption;
import vn.enlearning.backend.seed.SeedFiles.SeedPrompt;
import vn.enlearning.backend.seed.SeedFiles.SeedQuestion;
import vn.enlearning.backend.seed.SeedFiles.SeedReading;
import vn.enlearning.backend.seed.SeedFiles.SeedSpeaking;
import vn.enlearning.backend.seed.SeedFiles.SeedTopic;
import vn.enlearning.backend.seed.SeedFiles.SeedUser;
import vn.enlearning.backend.seed.SeedFiles.SeedWriting;

/**
 * Nạp dữ liệu mẫu từ {@code resources/seed/*.json}. Chỉ tồn tại ở profile {@code dev} và {@code test} để
 * tài khoản {@code admin / 123456} không bao giờ theo bản build production.
 *
 * <p>Toàn bộ chạy trong một giao dịch và chỉ khi bảng {@code users} trống, nên chạy lại không nhân đôi
 * dữ liệu và một file JSON lỗi không để lại nửa vời.
 */
@Slf4j
@Service
@Profile({ "dev", "test" })
@RequiredArgsConstructor
public class SeedService {

	private static final String RESOURCE_DIR = "seed/";

	/** Chặt: JSON có trường lạ (gõ sai tên) phải lỗi ngay, không được lặng lẽ bị bỏ qua. */
	private static final JsonMapper MAPPER = JsonMapper.builder()
			.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build();

	private final UserRepository users;
	private final UserSettingRepository settings;
	private final PasswordEncoder passwordEncoder;
	private final TopicRepository topics;
	private final FlashcardDeckRepository decks;
	private final FlashcardRepository flashcards;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final SpeakingLessonRepository speakingLessons;
	private final WritingPromptRepository writingPrompts;
	private final ExamRepository exams;
	private final QuestionRepository questions;

	/** Số dòng đã nạp; test và log đối chiếu với số lượng trong file JSON. */
	public record Report(int users, int topics, int decks, int flashcards, int listeningLessons,
			int readingLessons, int speakingLessons, int writingPrompts, int exams, int questions, int options) {
	}

	/** @return báo cáo nếu đã nạp; rỗng nếu DB đã có người dùng nên bỏ qua */
	@Transactional
	public Optional<Report> seedIfEmpty() {
		if (users.countIncludingDeleted() > 0) {
			log.info("DB đã có người dùng, bỏ qua nạp dữ liệu mẫu.");
			return Optional.empty();
		}

		User admin = null;
		int userCount = 0;
		for (SeedUser seed : read("users.json", SeedUser[].class)) {
			User user = new User();
			user.setEmail(Emails.normalize(seed.email()));
			user.setPasswordHash(passwordEncoder.encode(seed.password()));
			user.setFullName(seed.fullName());
			user.setRole(seed.role());
			user.setStatus(seed.status());
			users.save(user);

			UserSetting setting = new UserSetting();
			setting.setUser(user);
			settings.save(setting);

			if (seed.role() == Role.ADMIN && admin == null) {
				admin = user;
			}
			userCount++;
		}

		Map<String, Topic> topicBySlug = new HashMap<>();
		for (SeedTopic seed : read("topics.json", SeedTopic[].class)) {
			Topic topic = new Topic();
			topic.setSlug(seed.slug());
			topic.setNameVi(seed.nameVi());
			topic.setNameEn(seed.nameEn());
			topicBySlug.put(seed.slug(), topics.save(topic));
		}

		Counter counter = new Counter();
		seedDecks(topicBySlug, admin, counter);
		seedListening(topicBySlug, admin, counter);
		seedReading(topicBySlug, admin, counter);
		seedSpeaking(topicBySlug, admin, counter);
		seedWriting(topicBySlug, admin, counter);
		seedExams(admin, counter);

		Report report = new Report(userCount, topicBySlug.size(), counter.decks, counter.flashcards,
				counter.listening, counter.reading, counter.speaking, counter.writing, counter.exams,
				counter.questions, counter.options);
		log.info("Đã nạp dữ liệu mẫu: {}", report);
		return Optional.of(report);
	}

	private void seedDecks(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (SeedDeck seed : read("decks.json", SeedDeck[].class)) {
			FlashcardDeck deck = new FlashcardDeck();
			applyContent(deck, admin, seed.titleVi(), seed.titleEn(), seed.level());
			deck.setTopic(topic(topicBySlug, seed.topic()));
			deck.setDescriptionVi(seed.descriptionVi());
			deck.setDescriptionEn(seed.descriptionEn());
			deck.setCoverImageUrl(seed.coverImageUrl());
			decks.save(deck);

			List<Flashcard> cards = new ArrayList<>();
			for (SeedCard card : seed.cards()) {
				Flashcard flashcard = new Flashcard();
				flashcard.setDeck(deck);
				flashcard.setWord(card.word());
				flashcard.setPhonetic(card.phonetic());
				flashcard.setMeaningVi(card.meaningVi());
				flashcard.setMeaningEn(card.meaningEn());
				flashcard.setPartOfSpeechVi(card.partOfSpeechVi());
				flashcard.setPartOfSpeechEn(card.partOfSpeechEn());
				flashcard.setExample(card.example());
				flashcard.setExampleMeaning(card.exampleMeaning());
				flashcard.setImageUrl(card.imageUrl());
				flashcard.setAudioUrl(card.audioUrl());
				flashcard.setSortOrder(cards.size() + 1);
				cards.add(flashcard);
			}
			flashcards.saveAll(cards);
			counter.decks++;
			counter.flashcards += cards.size();
		}
	}

	private void seedListening(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (SeedListening seed : read("listening.json", SeedListening[].class)) {
			ListeningLesson lesson = new ListeningLesson();
			applyContent(lesson, admin, seed.titleVi(), seed.titleEn(), seed.level());
			lesson.setTopic(topic(topicBySlug, seed.topic()));
			lesson.setDescriptionVi(seed.descriptionVi());
			lesson.setDescriptionEn(seed.descriptionEn());
			lesson.setAudioUrl(seed.audioUrl());
			lesson.setDurationSeconds(seed.durationSeconds());
			lesson.setTranscript(seed.transcript());
			listeningLessons.save(lesson);

			saveQuestions(seed.questions(), question -> question.setListeningLesson(lesson), counter);
			counter.listening++;
		}
	}

	private void seedReading(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (SeedReading seed : read("reading.json", SeedReading[].class)) {
			ReadingLesson lesson = new ReadingLesson();
			applyContent(lesson, admin, seed.titleVi(), seed.titleEn(), seed.level());
			lesson.setTopic(topic(topicBySlug, seed.topic()));
			lesson.setDescriptionVi(seed.descriptionVi());
			lesson.setDescriptionEn(seed.descriptionEn());
			lesson.setTimeLimitMinutes(seed.timeLimitMinutes());
			lesson.setParagraphs(new ArrayList<>(seed.paragraphs()));
			// Cột này luôn do backend tính lại từ đoạn văn, không tin số trong mock.
			lesson.setWordCount(countWords(seed.paragraphs()));
			readingLessons.save(lesson);

			saveQuestions(seed.questions(), question -> question.setReadingLesson(lesson), counter);
			counter.reading++;
		}
	}

	private void seedSpeaking(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (SeedSpeaking seed : read("speaking.json", SeedSpeaking[].class)) {
			SpeakingLesson lesson = new SpeakingLesson();
			applyContent(lesson, admin, seed.titleVi(), seed.titleEn(), seed.level());
			lesson.setTopic(topic(topicBySlug, seed.topic()));
			lesson.setDescriptionVi(seed.descriptionVi());
			lesson.setDescriptionEn(seed.descriptionEn());
			List<SpeakingPrompt> prompts = new ArrayList<>();
			for (SeedPrompt prompt : seed.prompts()) {
				// Mỗi câu có id ổn định để lượt nói về sau tham chiếu được (xem ARCHITECTURE.md).
				prompts.add(new SpeakingPrompt(UUID.randomUUID(), prompt.text(), prompt.phonetic(), prompt.meaningVi()));
			}
			lesson.setPrompts(prompts);
			speakingLessons.save(lesson);
			counter.speaking++;
		}
	}

	private void seedWriting(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (SeedWriting seed : read("writing.json", SeedWriting[].class)) {
			WritingPrompt prompt = new WritingPrompt();
			applyContent(prompt, admin, seed.titleVi(), seed.titleEn(), seed.level());
			prompt.setTopic(topic(topicBySlug, seed.topic()));
			prompt.setInstructions(seed.instructions());
			prompt.setSuggestedMinutes(seed.suggestedMinutes());
			prompt.setMinWords(seed.minWords());
			prompt.setHints(new ArrayList<>(seed.hints()));
			writingPrompts.save(prompt);
			counter.writing++;
		}
	}

	private void seedExams(User admin, Counter counter) {
		for (SeedExam seed : read("exams.json", SeedExam[].class)) {
			Exam exam = new Exam();
			applyContent(exam, admin, seed.titleVi(), seed.titleEn(), seed.level());
			exam.setDescriptionVi(seed.descriptionVi());
			exam.setDescriptionEn(seed.descriptionEn());
			exam.setTimeLimitMinutes(seed.timeLimitMinutes());
			exams.save(exam);

			saveQuestions(seed.questions(), question -> question.setExam(exam), counter);
			counter.exams++;
		}
	}

	/** Mỗi câu thuộc đúng một chủ sở hữu (bài nghe, bài đọc hoặc đề thi) do {@code owner} gán. */
	private void saveQuestions(List<SeedQuestion> seeds, Consumer<Question> owner, Counter counter) {
		List<Question> built = new ArrayList<>();
		for (SeedQuestion seed : seeds) {
			Question question = new Question();
			owner.accept(question);
			question.setSkill(seed.skill());
			question.setKind(seed.kind());
			question.setSortOrder(built.size() + 1);
			question.setContent(seed.content());
			question.setExplanation(seed.explanation());
			question.setAcceptedAnswers(new ArrayList<>(seed.acceptedAnswers()));
			for (SeedOption option : seed.options()) {
				QuestionOption entity = new QuestionOption();
				entity.setQuestion(question);
				entity.setSortOrder(question.getOptions().size() + 1);
				entity.setContent(option.content());
				entity.setCorrect(option.correct());
				question.getOptions().add(entity);
				counter.options++;
			}
			built.add(question);
		}
		questions.saveAll(built);
		counter.questions += built.size();
	}

	private static void applyContent(ContentEntity content, User createdBy, String titleVi, String titleEn, Level level) {
		content.setTitleVi(titleVi);
		content.setTitleEn(titleEn);
		content.setLevel(level);
		content.setCreatedBy(createdBy);
	}

	private static Topic topic(Map<String, Topic> topicBySlug, String slug) {
		Topic topic = topicBySlug.get(slug);
		if (topic == null) {
			throw new IllegalStateException("File seed tham chiếu chủ đề không tồn tại: " + slug);
		}
		return topic;
	}

	private static int countWords(List<String> paragraphs) {
		return paragraphs.stream()
				.map(String::trim)
				.filter(paragraph -> !paragraph.isEmpty())
				.mapToInt(paragraph -> paragraph.split("\\s+").length)
				.sum();
	}

	private static <T> List<T> read(String file, Class<T[]> type) {
		try (InputStream input = new ClassPathResource(RESOURCE_DIR + file).getInputStream()) {
			return Arrays.asList(MAPPER.readValue(input, type));
		} catch (IOException e) {
			throw new UncheckedIOException("Không đọc được file seed " + file, e);
		}
	}

	/** Bộ đếm cục bộ của một lần chạy, để dựng {@link Report}. */
	private static final class Counter {
		int decks;
		int flashcards;
		int listening;
		int reading;
		int speaking;
		int writing;
		int exams;
		int questions;
		int options;
	}
}
