package vn.enlearning.backend.admin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminContentDetailResponse;
import vn.enlearning.backend.admin.dto.AdminContentRequest;
import vn.enlearning.backend.admin.dto.AdminContentRequest.CardInput;
import vn.enlearning.backend.admin.dto.AdminContentRequest.OptionInput;
import vn.enlearning.backend.admin.dto.AdminContentRequest.PromptInput;
import vn.enlearning.backend.admin.dto.AdminContentRequest.QuestionInput;
import vn.enlearning.backend.admin.dto.AdminContentSummaryResponse;
import vn.enlearning.backend.admin.dto.ContentType;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.QuestionRepository.ParentCount;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.WritingPrompt;

/** Đổi entity nội dung sang dòng danh sách và sang body PUT (kèm id phần tử con) để Admin sửa. */
@Component
@RequiredArgsConstructor
class AdminContentReader {

	private final FlashcardRepository cards;
	private final QuestionRepository questions;
	private final ContentUsage usage;

	static ContentType typeOf(ContentEntity entity) {
		return switch (entity) {
			case FlashcardDeck d -> ContentType.VOCABULARY;
			case ListeningLesson l -> ContentType.LISTENING;
			case ReadingLesson r -> ContentType.READING;
			case WritingPrompt w -> ContentType.WRITING;
			case SpeakingLesson s -> ContentType.SPEAKING;
			case Exam e -> ContentType.EXAM;
			default -> throw new IllegalArgumentException("Loại nội dung không hỗ trợ: " + entity.getClass());
		};
	}

	/** Số câu hỏi/thẻ/câu nói và trạng thái "đã dùng" được tính gộp theo loại để tránh truy vấn từng dòng. */
	List<AdminContentSummaryResponse> summaries(List<? extends ContentEntity> entities) {
		Map<ContentType, List<UUID>> idsByType = new HashMap<>();
		for (ContentEntity e : entities) {
			idsByType.computeIfAbsent(typeOf(e), t -> new ArrayList<>()).add(e.getId());
		}
		Map<UUID, Integer> counts = new HashMap<>();
		Set<UUID> used = new HashSet<>();
		idsByType.forEach((type, ids) -> {
			used.addAll(usage.used(type, ids));
			switch (type) {
				case VOCABULARY -> cards.countByDeckIds(ids).forEach(c -> counts.put(c.getDeckId(), (int) c.getTotal()));
				case LISTENING -> questions.countByListeningLessons(ids).forEach(c -> put(counts, c));
				case READING -> questions.countByReadingLessons(ids).forEach(c -> put(counts, c));
				case EXAM -> questions.countByExams(ids).forEach(c -> counts.merge(c.getParentId(), (int) c.getTotal(), Integer::sum));
				default -> {
				}
			}
		});
		return entities.stream().map(e -> summary(e, counts, used)).toList();
	}

	AdminContentDetailResponse detail(ContentEntity entity) {
		return AdminContentDetailResponse.of(summaries(List.of(entity)).get(0), payload(entity));
	}

	private static void put(Map<UUID, Integer> counts, ParentCount c) {
		counts.put(c.getParentId(), (int) c.getTotal());
	}

	private static AdminContentSummaryResponse summary(ContentEntity e, Map<UUID, Integer> counts, Set<UUID> used) {
		ContentType type = typeOf(e);
		int itemCount = switch (type) {
			case SPEAKING -> ((SpeakingLesson) e).getPrompts().size();
			case WRITING -> 1;
			default -> counts.getOrDefault(e.getId(), 0);
		};
		Topic topic = topicOf(e);
		return new AdminContentSummaryResponse(e.getId(), type, L10n.of(e.getTitleVi(), e.getTitleEn()),
				topic == null ? null : L10n.of(topic.getNameVi(), topic.getNameEn()), e.getLevel(), e.getStatus(),
				itemCount, e.getUpdatedAt(), used.contains(e.getId()));
	}

	private static Topic topicOf(ContentEntity e) {
		return switch (e) {
			case FlashcardDeck d -> d.getTopic();
			case ListeningLesson l -> l.getTopic();
			case ReadingLesson r -> r.getTopic();
			case WritingPrompt w -> w.getTopic();
			case SpeakingLesson s -> s.getTopic();
			default -> null;
		};
	}

	private AdminContentRequest payload(ContentEntity entity) {
		return switch (entity) {
			case FlashcardDeck d -> new AdminContentRequest.Vocabulary(d.getTitleVi(), d.getTitleEn(), d.getLevel(),
					d.getTopic().getId(), d.getDescriptionVi(), d.getDescriptionEn(), d.getCoverImageUrl(),
					cards.findByDeckIdOrderBySortOrderAscIdAsc(d.getId()).stream()
							.map(c -> new CardInput(c.getId(), c.getWord(), c.getPhonetic(), c.getMeaningVi(),
									c.getMeaningEn(), c.getPartOfSpeechVi(), c.getPartOfSpeechEn(), c.getExample(),
									c.getExampleMeaning(), c.getImageUrl(), c.getAudioUrl()))
							.toList());
			case ListeningLesson l -> new AdminContentRequest.Listening(l.getTitleVi(), l.getTitleEn(), l.getLevel(),
					l.getTopic().getId(), l.getDescriptionVi(), l.getDescriptionEn(), l.getAudioUrl(),
					l.getDurationSeconds(), l.getTranscript(),
					questionInputs(questions.findByListeningLessonIdOrderBySortOrder(l.getId())));
			case ReadingLesson r -> new AdminContentRequest.Reading(r.getTitleVi(), r.getTitleEn(), r.getLevel(),
					r.getTopic().getId(), r.getDescriptionVi(), r.getDescriptionEn(), r.getTimeLimitMinutes(),
					List.copyOf(r.getParagraphs()), questionInputs(questions.findByReadingLessonIdOrderBySortOrder(r.getId())));
			case WritingPrompt w -> new AdminContentRequest.Writing(w.getTitleVi(), w.getTitleEn(), w.getLevel(),
					w.getTopic().getId(), w.getInstructions(), w.getSuggestedMinutes(), w.getMinWords(),
					List.copyOf(w.getHints()));
			case SpeakingLesson s -> new AdminContentRequest.Speaking(s.getTitleVi(), s.getTitleEn(), s.getLevel(),
					s.getTopic().getId(), s.getDescriptionVi(), s.getDescriptionEn(),
					s.getPrompts().stream().map(p -> new PromptInput(p.id(), p.text(), p.phonetic(), p.meaningVi())).toList());
			case Exam x -> new AdminContentRequest.Exam(x.getTitleVi(), x.getTitleEn(), x.getLevel(), x.getDescriptionVi(),
					x.getDescriptionEn(), x.getTimeLimitMinutes(),
					questionInputs(questions.findByExamIdOrderBySortOrder(x.getId())));
			default -> throw new IllegalArgumentException("Loại nội dung không hỗ trợ: " + entity.getClass());
		};
	}

	private static List<QuestionInput> questionInputs(List<Question> list) {
		return list.stream()
				.map(q -> new QuestionInput(q.getId(), q.getSkill(), q.getKind(), q.getContent(), q.getExplanation(),
						q.getOptions().stream().map(o -> new OptionInput(o.getContent(), o.isCorrect())).toList(),
						List.copyOf(q.getAcceptedAnswers())))
				.toList();
	}
}
