package vn.enlearning.backend.seed;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.ContentEntity;
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
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.enums.AudioSource;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.seed.RealSeedFiles.RealListening;
import vn.enlearning.backend.seed.RealSeedFiles.RealReading;
import vn.enlearning.backend.seed.RealSeedFiles.RealSpeaking;
import vn.enlearning.backend.seed.RealSeedFiles.RealTopic;
import vn.enlearning.backend.seed.RealSeedFiles.RealWord;
import vn.enlearning.backend.seed.RealSeedFiles.RealWriting;
import vn.enlearning.backend.seed.SeedFiles.SeedOption;
import vn.enlearning.backend.seed.SeedFiles.SeedPrompt;
import vn.enlearning.backend.seed.SeedFiles.SeedQuestion;

/**
 * Nạp dữ liệu thật (đợt 13) từ hai nguồn (sinh bởi {@code tools/crawler}, xem
 * {@code .docs/roadmap/dot-13-du-lieu-that.md}): {@code resources/seed/real/*.json} (chủ đề, từ vựng, 8 bài
 * nghe VOA) và {@code resources/seed/{reading,writing,speaking}.json} (bài seed cũ, thu gọn, gắn thêm ảnh
 * minh hoạ ở đợt 13.7). Bật bằng {@code app.seed.real-data=true} ({@code REAL_DATA_SEED}).
 *
 * <p>Idempotent: mỗi loại có một khoá để bỏ qua bản ghi đã có (chủ đề theo {@code slug}, từ theo {@code word}
 * trong đúng bộ thẻ của chủ đề, bài Nghe/Đọc/Viết/Nói theo {@code titleVi} trong đúng chủ đề), nên chạy lại
 * nhiều lần không nhân đôi dữ liệu.
 */
@Slf4j
@Service
@Profile({ "dev", "test" })
@RequiredArgsConstructor
public class RealDataSeedService {

	private static final String RESOURCE_DIR = "seed/real/";
	private static final String SEED_DIR = "seed/";

	private static final JsonMapper MAPPER = JsonMapper.builder()
			.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build();

	private final UserRepository users;
	private final TopicRepository topics;
	private final FlashcardDeckRepository decks;
	private final FlashcardRepository flashcards;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final WritingPromptRepository writingPrompts;
	private final SpeakingLessonRepository speakingLessons;
	private final QuestionRepository questions;

	/** Số bản ghi đã tạo mới / bỏ qua (đã có từ lần chạy trước), để log và test đối chiếu. */
	public record Report(int topicsCreated, int topicsSkipped, int wordsCreated, int wordsSkipped,
			int listeningCreated, int listeningSkipped, int readingCreated, int readingSkipped,
			int writingCreated, int writingSkipped, int speakingCreated, int speakingSkipped, int questionsCreated) {
	}

	@Transactional
	public Report seed() {
		User admin = users.findFirstByRoleOrderByCreatedAtAsc(Role.ADMIN).orElse(null);
		Counter counter = new Counter();

		Map<String, Topic> topicBySlug = seedTopics(counter);
		seedWords(topicBySlug, admin, counter);
		seedListening(topicBySlug, admin, counter);
		seedReading(topicBySlug, admin, counter);
		seedWriting(topicBySlug, admin, counter);
		seedSpeaking(topicBySlug, admin, counter);

		Report report = new Report(counter.topicsCreated, counter.topicsSkipped, counter.wordsCreated,
				counter.wordsSkipped, counter.listeningCreated, counter.listeningSkipped, counter.readingCreated,
				counter.readingSkipped, counter.writingCreated, counter.writingSkipped, counter.speakingCreated,
				counter.speakingSkipped, counter.questions);
		log.info("Đã nạp dữ liệu thật (đợt 13): {}", report);
		return report;
	}

	private Map<String, Topic> seedTopics(Counter counter) {
		Map<String, Topic> byTopic = new LinkedHashMap<>();
		for (RealTopic seed : readReal("topics.json", RealTopic[].class)) {
			Topic topic = topics.findBySlug(seed.slug()).orElse(null);
			if (topic != null) {
				// Ảnh có thể được gắn thêm sau khi chủ đề đã tồn tại (đợt 13.4 chạy lại); cập nhật một lần.
				if (seed.imageUrl() != null && topic.getImageUrl() == null) {
					topic.setImageUrl(seed.imageUrl());
					topic.setImageAuthor(seed.imageAuthor());
					topic.setImageAuthorUrl(seed.imageAuthorUrl());
					topics.save(topic);
				}
				byTopic.put(seed.slug(), topic);
				counter.topicsSkipped++;
				continue;
			}
			topic = new Topic();
			topic.setSlug(seed.slug());
			topic.setNameVi(seed.nameVi());
			topic.setNameEn(seed.nameEn());
			topic.setImageUrl(seed.imageUrl());
			topic.setImageAuthor(seed.imageAuthor());
			topic.setImageAuthorUrl(seed.imageAuthorUrl());
			byTopic.put(seed.slug(), topics.save(topic));
			counter.topicsCreated++;
		}
		return byTopic;
	}

	/** Từ vựng phẳng theo {@code topicSlug} được gom vào đúng một bộ thẻ "thật" mỗi chủ đề. */
	private void seedWords(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		Map<String, List<RealWord>> byTopic = new LinkedHashMap<>();
		for (RealWord word : readReal("words.json", RealWord[].class)) {
			byTopic.computeIfAbsent(word.topicSlug(), key -> new ArrayList<>()).add(word);
		}

		for (Map.Entry<String, List<RealWord>> entry : byTopic.entrySet()) {
			Topic topic = topic(topicBySlug, entry.getKey());
			FlashcardDeck deck = decks.findByTopicIdAndTitleVi(topic.getId(), deckTitleVi(topic))
					.map(existingDeck -> backfillDeckCover(existingDeck, topic))
					.orElseGet(() -> createDeck(topic, admin));

			List<Flashcard> existing = flashcards.findByDeckIdOrderBySortOrderAscIdAsc(deck.getId());
			Map<String, Flashcard> existingByWord = new LinkedHashMap<>();
			for (Flashcard card : existing) {
				existingByWord.put(card.getWord().toLowerCase(Locale.ROOT), card);
			}
			int nextOrder = existing.size() + 1;

			List<Flashcard> newCards = new ArrayList<>();
			List<Flashcard> updatedCards = new ArrayList<>();
			for (RealWord seed : entry.getValue()) {
				Flashcard existingCard = existingByWord.get(seed.word().toLowerCase(Locale.ROOT));
				if (existingCard != null) {
					boolean changed = false;
					// Ảnh có thể được gắn thêm sau khi flashcard đã tồn tại (đợt 13.4 chạy lại); cập nhật một lần.
					if (seed.imageUrl() != null && existingCard.getImageUrl() == null) {
						existingCard.setImageUrl(seed.imageUrl());
						existingCard.setImageAuthor(seed.imageAuthor());
						existingCard.setImageAuthorUrl(seed.imageAuthorUrl());
						changed = true;
					}
					// Phiên âm ARPAbet cũ (nguồn Datamuse trước khi sửa dictionaryClient.js) -> nạp lại IPA đúng.
					if (seed.phonetic() != null && !seed.phonetic().equals(existingCard.getPhonetic())) {
						existingCard.setPhonetic(seed.phonetic());
						changed = true;
					}
					if (changed) {
						updatedCards.add(existingCard);
					}
					counter.wordsSkipped++;
					continue;
				}
				Flashcard card = new Flashcard();
				card.setDeck(deck);
				card.setWord(seed.word());
				card.setPhonetic(seed.phonetic());
				card.setMeaningVi(seed.meaningVi());
				card.setMeaningEn(seed.meaningEn());
				card.setPartOfSpeechVi(seed.partOfSpeechVi());
				card.setPartOfSpeechEn(seed.partOfSpeechEn());
				card.setExample(seed.example());
				card.setExampleMeaning(seed.exampleMeaning());
				card.setImageUrl(seed.imageUrl());
				card.setImageAuthor(seed.imageAuthor());
				card.setImageAuthorUrl(seed.imageAuthorUrl());
				card.setSortOrder(nextOrder++);
				newCards.add(card);
			}
			flashcards.saveAll(newCards);
			flashcards.saveAll(updatedCards);
			counter.wordsCreated += newCards.size();
		}
	}

	/** Ảnh chủ đề có thể được gắn thêm sau khi bộ thẻ đã tồn tại (đợt 13.4 chạy lại); cập nhật một lần. */
	private FlashcardDeck backfillDeckCover(FlashcardDeck deck, Topic topic) {
		if (topic.getImageUrl() != null && deck.getCoverImageUrl() == null) {
			deck.setCoverImageUrl(topic.getImageUrl());
			deck.setCoverImageAuthor(topic.getImageAuthor());
			deck.setCoverImageAuthorUrl(topic.getImageAuthorUrl());
			decks.save(deck);
		}
		return deck;
	}

	private FlashcardDeck createDeck(Topic topic, User admin) {
		FlashcardDeck deck = new FlashcardDeck();
		deck.setTopic(topic);
		deck.setTitleVi(deckTitleVi(topic));
		deck.setTitleEn(deckTitleEn(topic));
		deck.setLevel(Level.INTERMEDIATE);
		deck.setCreatedBy(admin);
		deck.setCoverImageUrl(topic.getImageUrl());
		deck.setCoverImageAuthor(topic.getImageAuthor());
		deck.setCoverImageAuthorUrl(topic.getImageAuthorUrl());
		return decks.save(deck);
	}

	private static String deckTitleVi(Topic topic) {
		return "Từ vựng thật: " + topic.getNameVi();
	}

	private static String deckTitleEn(Topic topic) {
		return "Real vocabulary: " + topic.getNameEn();
	}

	private void seedListening(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (RealListening seed : readReal("listening.json", RealListening[].class)) {
			Topic topic = topic(topicBySlug, seed.topic());
			if (listeningLessons.existsByTopicIdAndTitleVi(topic.getId(), seed.titleVi())) {
				counter.listeningSkipped++;
				continue;
			}
			ListeningLesson lesson = new ListeningLesson();
			applyContent(lesson, admin, seed.titleVi(), seed.titleEn(), seed.level());
			if (seed.status() != null) {
				lesson.setStatus(seed.status());
			}
			lesson.setTopic(topic);
			lesson.setDescriptionVi(seed.descriptionVi());
			lesson.setDescriptionEn(seed.descriptionEn());
			lesson.setImageUrl(seed.imageUrl());
			lesson.setImageAuthor(seed.imageAuthor());
			lesson.setImageAuthorUrl(seed.imageAuthorUrl());
			// audioUrl: tools/crawler/src/generateAudio.js (13.6) tải audio người đọc thật (Tatoeba) rồi ghi thẳng vào JSON.
			if (seed.audioUrl() != null) {
				lesson.setAudioUrl(seed.audioUrl());
				lesson.setAudioSource(AudioSource.REAL);
				lesson.setAudioPublicId(seed.audioPublicId());
				if (seed.durationSeconds() != null && seed.durationSeconds() > 0) {
					lesson.setDurationSeconds(seed.durationSeconds());
				}
			}
			lesson.setTranscript(seed.transcript());
			listeningLessons.save(lesson);

			saveQuestions(seed.questions(), question -> question.setListeningLesson(lesson), counter);
			counter.listeningCreated++;
		}
	}

	private void seedReading(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (RealReading seed : readSeed("reading.json", RealReading[].class)) {
			Topic topic = legacyTopic(topicBySlug, seed.topic());
			if (topic == null) {
				log.warn("Bỏ qua bài đọc '{}': chủ đề '{}' không khớp 20 chủ đề thật.", seed.titleVi(), seed.topic());
				continue;
			}
			ReadingLesson existingLesson = readingLessons.findByTopicIdAndTitleVi(topic.getId(), seed.titleVi())
					.orElse(null);
			if (existingLesson != null) {
				// Ảnh minh hoạ được gắn thêm sau khi bài đã tồn tại (đợt 13.7 chạy lại); cập nhật một lần.
				if (seed.imageUrl() != null && existingLesson.getImageUrl() == null) {
					existingLesson.setImageUrl(seed.imageUrl());
					existingLesson.setImageAuthor(seed.imageAuthor());
					existingLesson.setImageAuthorUrl(seed.imageAuthorUrl());
					readingLessons.save(existingLesson);
				}
				counter.readingSkipped++;
				continue;
			}
			ReadingLesson lesson = new ReadingLesson();
			applyContent(lesson, admin, seed.titleVi(), seed.titleEn(), seed.level());
			if (seed.status() != null) {
				lesson.setStatus(seed.status());
			}
			lesson.setTopic(topic);
			lesson.setDescriptionVi(seed.descriptionVi());
			lesson.setDescriptionEn(seed.descriptionEn());
			lesson.setImageUrl(seed.imageUrl());
			lesson.setImageAuthor(seed.imageAuthor());
			lesson.setImageAuthorUrl(seed.imageAuthorUrl());
			lesson.setTimeLimitMinutes(seed.timeLimitMinutes());
			lesson.setParagraphs(new ArrayList<>(seed.paragraphs()));
			lesson.setWordCount(countWords(seed.paragraphs()));
			readingLessons.save(lesson);

			saveQuestions(seed.questions(), question -> question.setReadingLesson(lesson), counter);
			counter.readingCreated++;
		}
	}

	private void seedWriting(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (RealWriting seed : readSeed("writing.json", RealWriting[].class)) {
			Topic topic = legacyTopic(topicBySlug, seed.topic());
			if (topic == null) {
				log.warn("Bỏ qua đề viết '{}': chủ đề '{}' không khớp 20 chủ đề thật.", seed.titleVi(), seed.topic());
				continue;
			}
			WritingPrompt existingPrompt = writingPrompts.findByTopicIdAndTitleVi(topic.getId(), seed.titleVi())
					.orElse(null);
			if (existingPrompt != null) {
				// Ảnh minh hoạ được gắn thêm sau khi đề đã tồn tại (đợt 13.7 chạy lại); cập nhật một lần.
				if (seed.imageUrl() != null && existingPrompt.getImageUrl() == null) {
					existingPrompt.setImageUrl(seed.imageUrl());
					existingPrompt.setImageAuthor(seed.imageAuthor());
					existingPrompt.setImageAuthorUrl(seed.imageAuthorUrl());
					writingPrompts.save(existingPrompt);
				}
				counter.writingSkipped++;
				continue;
			}
			WritingPrompt prompt = new WritingPrompt();
			applyContent(prompt, admin, seed.titleVi(), seed.titleEn(), seed.level());
			if (seed.status() != null) {
				prompt.setStatus(seed.status());
			}
			prompt.setTopic(topic);
			prompt.setImageUrl(seed.imageUrl());
			prompt.setImageAuthor(seed.imageAuthor());
			prompt.setImageAuthorUrl(seed.imageAuthorUrl());
			prompt.setInstructions(seed.instructions());
			prompt.setSuggestedMinutes(seed.suggestedMinutes());
			prompt.setMinWords(seed.minWords());
			prompt.setHints(new ArrayList<>(seed.hints()));
			writingPrompts.save(prompt);
			counter.writingCreated++;
		}
	}

	private void seedSpeaking(Map<String, Topic> topicBySlug, User admin, Counter counter) {
		for (RealSpeaking seed : readSeed("speaking.json", RealSpeaking[].class)) {
			Topic topic = legacyTopic(topicBySlug, seed.topic());
			if (topic == null) {
				log.warn("Bỏ qua bài nói '{}': chủ đề '{}' không khớp 20 chủ đề thật.", seed.titleVi(), seed.topic());
				continue;
			}
			SpeakingLesson existingLesson = speakingLessons.findByTopicIdAndTitleVi(topic.getId(), seed.titleVi())
					.orElse(null);
			if (existingLesson != null) {
				// Ảnh minh hoạ được gắn thêm sau khi bài đã tồn tại (đợt 13.7 chạy lại); cập nhật một lần.
				if (seed.imageUrl() != null && existingLesson.getImageUrl() == null) {
					existingLesson.setImageUrl(seed.imageUrl());
					existingLesson.setImageAuthor(seed.imageAuthor());
					existingLesson.setImageAuthorUrl(seed.imageAuthorUrl());
					speakingLessons.save(existingLesson);
				}
				counter.speakingSkipped++;
				continue;
			}
			SpeakingLesson lesson = new SpeakingLesson();
			applyContent(lesson, admin, seed.titleVi(), seed.titleEn(), seed.level());
			if (seed.status() != null) {
				lesson.setStatus(seed.status());
			}
			lesson.setTopic(topic);
			lesson.setDescriptionVi(seed.descriptionVi());
			lesson.setDescriptionEn(seed.descriptionEn());
			lesson.setImageUrl(seed.imageUrl());
			lesson.setImageAuthor(seed.imageAuthor());
			lesson.setImageAuthorUrl(seed.imageAuthorUrl());
			List<SpeakingPrompt> prompts = new ArrayList<>();
			for (SeedPrompt prompt : seed.prompts()) {
				prompts.add(new SpeakingPrompt(UUID.randomUUID(), prompt.text(), prompt.phonetic(), prompt.meaningVi()));
			}
			lesson.setPrompts(prompts);
			speakingLessons.save(lesson);
			counter.speakingCreated++;
		}
	}

	/** Mỗi câu thuộc đúng một chủ sở hữu (bài nghe hoặc bài đọc) do {@code owner} gán. */
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
			throw new IllegalStateException("File seed dữ liệu thật tham chiếu chủ đề không tồn tại: " + slug);
		}
		return topic;
	}

	/**
	 * {@code seed/{reading,writing,speaking}.json} là bài seed rất cũ, đặt slug chủ đề trước khi có danh sách
	 * 20 chủ đề thật ({@code tools/crawler/src/topics.js}) nên vài slug không khớp trực tiếp. Bảng bên dưới
	 * ánh xạ slug cũ đã biết sang slug thật; slug lạ khác thì bỏ qua bản ghi đó (log cảnh báo) thay vì chặn
	 * cả server khởi động.
	 */
	private static final Map<String, String> LEGACY_TOPIC_ALIASES = Map.of(
			"life", "daily-life",
			"toeic", "exam-prep",
			"ielts", "exam-prep",
			"work", "workplace",
			"foundation", "education");

	private static Topic legacyTopic(Map<String, Topic> topicBySlug, String slug) {
		Topic topic = topicBySlug.get(slug);
		if (topic != null) {
			return topic;
		}
		String alias = LEGACY_TOPIC_ALIASES.get(slug);
		return alias == null ? null : topicBySlug.get(alias);
	}

	private static int countWords(List<String> paragraphs) {
		return paragraphs.stream()
				.map(String::trim)
				.filter(paragraph -> !paragraph.isEmpty())
				.mapToInt(paragraph -> paragraph.split("\\s+").length)
				.sum();
	}

	private static <T> List<T> readReal(String file, Class<T[]> type) {
		return readResource(RESOURCE_DIR + file, type);
	}

	/** Đọc/Viết/Nói: bài seed cũ đã gắn ảnh (đợt 13.7), nằm cạnh {@code users.json} ở {@code resources/seed}. */
	private static <T> List<T> readSeed(String file, Class<T[]> type) {
		return readResource(SEED_DIR + file, type);
	}

	private static <T> List<T> readResource(String path, Class<T[]> type) {
		try (InputStream input = new ClassPathResource(path).getInputStream()) {
			return Arrays.asList(MAPPER.readValue(input, type));
		} catch (IOException e) {
			throw new UncheckedIOException("Không đọc được file seed " + path, e);
		}
	}

	/** Bộ đếm cục bộ của một lần chạy, để dựng {@link Report}. */
	private static final class Counter {
		int topicsCreated;
		int topicsSkipped;
		int wordsCreated;
		int wordsSkipped;
		int listeningCreated;
		int listeningSkipped;
		int readingCreated;
		int readingSkipped;
		int writingCreated;
		int writingSkipped;
		int speakingCreated;
		int speakingSkipped;
		int questions;
	}
}
