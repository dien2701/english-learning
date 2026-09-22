package vn.enlearning.backend.admin.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminContentRequest;
import vn.enlearning.backend.admin.dto.AdminContentRequest.CardInput;
import vn.enlearning.backend.admin.dto.AdminContentRequest.OptionInput;
import vn.enlearning.backend.admin.dto.AdminContentRequest.PromptInput;
import vn.enlearning.backend.admin.dto.AdminContentRequest.QuestionInput;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
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
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;

/**
 * Ghi nội dung do Admin gửi lên. Phần tử con (thẻ, câu hỏi, câu nói) khớp theo {@code id}: có id thì sửa tại chỗ,
 * không có thì tạo mới, vắng mặt thì xoá. Nội dung đã có trong lịch sử học ({@code inUse}) không được bớt phần tử con
 * vì lịch sử còn tham chiếu tới chúng. Phương án của câu trắc nghiệm khớp theo vị trí, nên không vướng ràng buộc
 * duy nhất (câu hỏi, thứ tự) khi đổi chỗ. Phần tử đã có chỉ sửa tại chỗ (dirty checking), không gọi save/merge;
 * chỉ phần tử mới được persist.
 */
@Component
@RequiredArgsConstructor
class AdminContentWriter {

	private static final int MAX_OPTIONS = 10;

	private final FlashcardDeckRepository decks;
	private final FlashcardRepository cards;
	private final WritingPromptRepository writingPrompts;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final SpeakingLessonRepository speakingLessons;
	private final ExamRepository exams;
	private final QuestionRepository questions;
	private final TopicRepository topics;
	private final UserRepository users;

	UUID create(UUID adminId, AdminContentRequest request) {
		var creator = users.getReferenceById(adminId);
		return switch (request) {
			case AdminContentRequest.Vocabulary v -> {
				FlashcardDeck deck = new FlashcardDeck();
				deck.setCreatedBy(creator);
				applyDeck(deck, v);
				decks.saveAndFlush(deck);
				syncCards(deck, v.cards(), false);
				yield deck.getId();
			}
			case AdminContentRequest.Listening l -> {
				ListeningLesson lesson = new ListeningLesson();
				lesson.setCreatedBy(creator);
				applyListening(lesson, l);
				listeningLessons.saveAndFlush(lesson);
				syncQuestions(List.of(), l.questions(), q -> q.setListeningLesson(lesson), Skill.LISTENING, false);
				yield lesson.getId();
			}
			case AdminContentRequest.Reading r -> {
				ReadingLesson lesson = new ReadingLesson();
				lesson.setCreatedBy(creator);
				applyReading(lesson, r);
				readingLessons.saveAndFlush(lesson);
				syncQuestions(List.of(), r.questions(), q -> q.setReadingLesson(lesson), Skill.READING, false);
				yield lesson.getId();
			}
			case AdminContentRequest.Writing w -> {
				WritingPrompt prompt = new WritingPrompt();
				prompt.setCreatedBy(creator);
				applyWriting(prompt, w);
				yield writingPrompts.saveAndFlush(prompt).getId();
			}
			case AdminContentRequest.Speaking s -> {
				SpeakingLesson lesson = new SpeakingLesson();
				lesson.setCreatedBy(creator);
				applySpeaking(lesson, s, false);
				yield speakingLessons.saveAndFlush(lesson).getId();
			}
			case AdminContentRequest.Exam e -> {
				Exam exam = new Exam();
				exam.setCreatedBy(creator);
				applyExam(exam, e);
				exams.saveAndFlush(exam);
				syncQuestions(List.of(), e.questions(), q -> q.setExam(exam), null, false);
				yield exam.getId();
			}
		};
	}

	void update(ContentEntity entity, AdminContentRequest request, boolean inUse) {
		switch (request) {
			case AdminContentRequest.Vocabulary v -> {
				FlashcardDeck deck = expect(entity, FlashcardDeck.class);
				applyDeck(deck, v);
				syncCards(deck, v.cards(), inUse);
			}
			case AdminContentRequest.Listening l -> {
				ListeningLesson lesson = expect(entity, ListeningLesson.class);
				applyListening(lesson, l);
				syncQuestions(questions.findByListeningLessonIdOrderBySortOrder(lesson.getId()), l.questions(),
						q -> q.setListeningLesson(lesson), Skill.LISTENING, inUse);
			}
			case AdminContentRequest.Reading r -> {
				ReadingLesson lesson = expect(entity, ReadingLesson.class);
				applyReading(lesson, r);
				syncQuestions(questions.findByReadingLessonIdOrderBySortOrder(lesson.getId()), r.questions(),
						q -> q.setReadingLesson(lesson), Skill.READING, inUse);
			}
			case AdminContentRequest.Writing w -> applyWriting(expect(entity, WritingPrompt.class), w);
			case AdminContentRequest.Speaking s -> applySpeaking(expect(entity, SpeakingLesson.class), s, inUse);
			case AdminContentRequest.Exam e -> {
				Exam exam = expect(entity, Exam.class);
				applyExam(exam, e);
				syncQuestions(questions.findByExamIdOrderBySortOrder(exam.getId()), e.questions(),
						q -> q.setExam(exam), null, inUse);
			}
		}
	}

	// --- trường của từng loại ---------------------------------------------------------------------

	private void applyBase(ContentEntity content, AdminContentRequest r) {
		content.setTitleVi(r.titleVi().trim());
		content.setTitleEn(blankToNull(r.titleEn()));
		content.setLevel(r.level());
	}

	private void applyDeck(FlashcardDeck deck, AdminContentRequest.Vocabulary v) {
		applyBase(deck, v);
		deck.setTopic(topic(v.topicId()));
		deck.setDescriptionVi(blankToNull(v.descriptionVi()));
		deck.setDescriptionEn(blankToNull(v.descriptionEn()));
		deck.setCoverImageUrl(blankToNull(v.coverImageUrl()));
	}

	private void applyListening(ListeningLesson lesson, AdminContentRequest.Listening l) {
		applyBase(lesson, l);
		lesson.setTopic(topic(l.topicId()));
		lesson.setDescriptionVi(blankToNull(l.descriptionVi()));
		lesson.setDescriptionEn(blankToNull(l.descriptionEn()));
		// Audio do TTS/tải lên được quản lý bằng /admin/listening/{id}/audio; PUT không được đè hay xoá nó.
		if (lesson.getAudioSource() == null) {
			lesson.setAudioUrl(blankToNull(l.audioUrl()));
		}
		lesson.setDurationSeconds(orElse(l.durationSeconds(), 0));
		lesson.setTranscript(l.transcript().trim());
	}

	private void applyReading(ReadingLesson lesson, AdminContentRequest.Reading r) {
		applyBase(lesson, r);
		lesson.setTopic(topic(r.topicId()));
		lesson.setDescriptionVi(blankToNull(r.descriptionVi()));
		lesson.setDescriptionEn(blankToNull(r.descriptionEn()));
		lesson.setTimeLimitMinutes(orElse(r.timeLimitMinutes(), 0));
		List<String> paragraphs = r.paragraphs().stream().map(String::trim).toList();
		lesson.setParagraphs(new ArrayList<>(paragraphs));
		lesson.setWordCount(paragraphs.stream().mapToInt(AdminContentWriter::countWords).sum());
	}

	private void applyWriting(WritingPrompt prompt, AdminContentRequest.Writing w) {
		applyBase(prompt, w);
		prompt.setTopic(topic(w.topicId()));
		prompt.setInstructions(w.instructions().trim());
		prompt.setSuggestedMinutes(orElse(w.suggestedMinutes(), 30));
		prompt.setMinWords(orElse(w.minWords(), 0));
		List<String> hints = w.hints() == null ? List.of() : w.hints().stream().map(String::trim).toList();
		prompt.setHints(new ArrayList<>(hints));
	}

	private void applyExam(Exam exam, AdminContentRequest.Exam e) {
		applyBase(exam, e);
		exam.setDescriptionVi(blankToNull(e.descriptionVi()));
		exam.setDescriptionEn(blankToNull(e.descriptionEn()));
		exam.setTimeLimitMinutes(orElse(e.timeLimitMinutes(), 0));
	}

	private void applySpeaking(SpeakingLesson lesson, AdminContentRequest.Speaking s, boolean inUse) {
		applyBase(lesson, s);
		lesson.setTopic(topic(s.topicId()));
		lesson.setDescriptionVi(blankToNull(s.descriptionVi()));
		lesson.setDescriptionEn(blankToNull(s.descriptionEn()));

		Set<UUID> existing = lesson.getPrompts().stream().map(SpeakingPrompt::id).collect(Collectors.toSet());
		Set<UUID> kept = new HashSet<>();
		List<SpeakingPrompt> prompts = new ArrayList<>();
		for (int i = 0; i < s.prompts().size(); i++) {
			PromptInput in = s.prompts().get(i);
			UUID id = in.id();
			if (id == null) {
				id = UUID.randomUUID();
			} else if (!existing.contains(id) || !kept.add(id)) {
				throw itemInvalid("prompts[" + i + "].id");
			}
			prompts.add(new SpeakingPrompt(id, in.text().trim(), blankToNull(in.phonetic()), blankToNull(in.meaningVi())));
		}
		if (inUse && !kept.containsAll(existing)) {
			throw new ApiException(ErrorCode.CONTENT_IN_USE);
		}
		lesson.setPrompts(prompts);
	}

	// --- phần tử con ------------------------------------------------------------------------------

	private void syncCards(FlashcardDeck deck, List<CardInput> inputs, boolean inUse) {
		List<Flashcard> existing = cards.findByDeckIdOrderBySortOrderAscIdAsc(deck.getId());
		Map<UUID, Flashcard> byId = existing.stream().collect(Collectors.toMap(Flashcard::getId, Function.identity()));
		Set<UUID> kept = new HashSet<>();
		List<Flashcard> created = new ArrayList<>();
		for (int i = 0; i < inputs.size(); i++) {
			CardInput in = inputs.get(i);
			Flashcard card;
			if (in.id() == null) {
				card = new Flashcard();
				card.setDeck(deck);
				created.add(card);
			} else {
				card = byId.get(in.id());
				if (card == null || !kept.add(card.getId())) {
					throw itemInvalid("cards[" + i + "].id");
				}
			}
			card.setWord(in.word().trim());
			card.setPhonetic(blankToNull(in.phonetic()));
			card.setMeaningVi(in.meaningVi().trim());
			card.setMeaningEn(blankToNull(in.meaningEn()));
			card.setPartOfSpeechVi(blankToNull(in.partOfSpeechVi()));
			card.setPartOfSpeechEn(blankToNull(in.partOfSpeechEn()));
			card.setExample(blankToNull(in.example()));
			card.setExampleMeaning(blankToNull(in.exampleMeaning()));
			card.setImageUrl(blankToNull(in.imageUrl()));
			card.setAudioUrl(blankToNull(in.audioUrl()));
			card.setSortOrder(i + 1);
		}
		List<Flashcard> removed = existing.stream().filter(c -> !kept.contains(c.getId())).toList();
		if (!removed.isEmpty()) {
			if (inUse) {
				throw new ApiException(ErrorCode.CONTENT_IN_USE);
			}
			cards.deleteAll(removed);
		}
		cards.saveAll(created);
	}

	/** {@code fixedSkill} null nghĩa là đề kiểm tra: kỹ năng lấy từ từng câu hỏi. */
	private void syncQuestions(List<Question> existing, List<QuestionInput> inputs, Consumer<Question> attach,
			Skill fixedSkill, boolean inUse) {
		Map<UUID, Question> byId = existing.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
		Set<UUID> kept = new HashSet<>();
		List<Question> created = new ArrayList<>();
		for (int i = 0; i < inputs.size(); i++) {
			QuestionInput in = inputs.get(i);
			String at = "questions[" + i + "]";
			Question question;
			if (in.id() == null) {
				question = new Question();
				attach.accept(question);
				created.add(question);
			} else {
				question = byId.get(in.id());
				if (question == null || !kept.add(question.getId())) {
					throw itemInvalid(at + ".id");
				}
			}
			Skill skill = fixedSkill != null ? fixedSkill : in.skill();
			if (skill == null) {
				throw ApiException.field(ErrorCode.VALIDATION, at + ".skill", "errors.field.skillRequired");
			}
			question.setSkill(skill);
			question.setKind(in.kind());
			question.setSortOrder(i + 1);
			question.setContent(in.content().trim());
			question.setExplanation(blankToNull(in.explanation()));
			if (in.kind() == QuestionKind.SINGLE_CHOICE) {
				applyOptions(question, in.options(), at);
				question.setAcceptedAnswers(new ArrayList<>());
			} else {
				question.getOptions().clear();
				question.setAcceptedAnswers(acceptedAnswers(in.acceptedAnswers(), at));
			}
		}
		List<Question> removed = existing.stream().filter(q -> !kept.contains(q.getId())).toList();
		if (!removed.isEmpty()) {
			if (inUse) {
				throw new ApiException(ErrorCode.CONTENT_IN_USE);
			}
			questions.deleteAll(removed);
		}
		questions.saveAll(created);
	}

	private static void applyOptions(Question question, List<OptionInput> inputs, String at) {
		if (inputs == null || inputs.size() < 2 || inputs.size() > MAX_OPTIONS) {
			throw ApiException.field(ErrorCode.VALIDATION, at + ".options", "errors.field.optionsCount");
		}
		if (inputs.stream().filter(o -> Boolean.TRUE.equals(o.correct())).count() != 1) {
			throw ApiException.field(ErrorCode.VALIDATION, at + ".options", "errors.field.oneCorrectOption");
		}
		List<QuestionOption> options = question.getOptions();
		for (int i = 0; i < inputs.size(); i++) {
			QuestionOption option;
			if (i < options.size()) {
				option = options.get(i);
			} else {
				option = new QuestionOption();
				option.setQuestion(question);
				options.add(option);
			}
			option.setSortOrder(i + 1);
			option.setContent(inputs.get(i).content().trim());
			option.setCorrect(Boolean.TRUE.equals(inputs.get(i).correct()));
		}
		while (options.size() > inputs.size()) {
			options.remove(options.size() - 1);
		}
	}

	private static List<String> acceptedAnswers(List<String> inputs, String at) {
		List<String> answers = inputs == null ? List.of() : inputs.stream().map(String::trim).toList();
		if (answers.isEmpty()) {
			throw ApiException.field(ErrorCode.VALIDATION, at + ".acceptedAnswers", "errors.field.acceptedAnswersRequired");
		}
		return new ArrayList<>(answers);
	}

	// --- tiện ích ---------------------------------------------------------------------------------

	private Topic topic(UUID id) {
		return topics.findById(id)
				.orElseThrow(() -> ApiException.field(ErrorCode.VALIDATION, "topicId", "errors.field.topicNotFound"));
	}

	private static <T> T expect(ContentEntity entity, Class<T> type) {
		if (!type.isInstance(entity)) {
			throw ApiException.field(ErrorCode.VALIDATION, "skill", "errors.field.skillMismatch");
		}
		return type.cast(entity);
	}

	private static ApiException itemInvalid(String field) {
		return ApiException.field(ErrorCode.VALIDATION, field, "errors.field.itemNotFound");
	}

	private static int orElse(Integer value, int fallback) {
		return value == null ? fallback : value;
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private static int countWords(String text) {
		String trimmed = text.trim();
		return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
	}
}
