package vn.enlearning.backend.practice.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository.ParentScore;
import vn.enlearning.backend.content.repository.QuestionRepository;
import vn.enlearning.backend.content.repository.QuestionRepository.ExamSkillCount;
import vn.enlearning.backend.content.repository.QuestionRepository.ParentCount;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.QuestionKind;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.practice.dto.ExamDetailResponse;
import vn.enlearning.backend.practice.dto.ExamSummaryResponse;
import vn.enlearning.backend.practice.dto.ListeningDetailResponse;
import vn.enlearning.backend.practice.dto.ListeningSummaryResponse;
import vn.enlearning.backend.practice.dto.QuestionOptionResponse;
import vn.enlearning.backend.practice.dto.QuestionResponse;
import vn.enlearning.backend.practice.dto.ReadingDetailResponse;
import vn.enlearning.backend.practice.dto.ReadingSummaryResponse;

/**
 * Danh sách và chi tiết bài Nghe, Đọc và đề Kiểm tra cho người học. Chi tiết KHÔNG bao giờ chứa đáp án đúng,
 * giải thích, đáp án điền từ hay transcript: những thứ đó chỉ có trong kết quả sau khi nộp. Người học chỉ thấy
 * nội dung ACTIVE. {@code isCompleted}/{@code status}/{@code lastScore} lấy từ lượt làm của chính người dùng.
 */
@Service
@RequiredArgsConstructor
public class PracticeCatalogService {

	static final String COMPLETED = "COMPLETED";
	static final String NOT_COMPLETED = "NOT_COMPLETED";
	static final String NOT_TAKEN = "NOT_TAKEN";
	static final String IN_PROGRESS = "IN_PROGRESS";
	private static final Set<String> LESSON_STATUSES = Set.of(COMPLETED, NOT_COMPLETED);
	private static final Set<String> EXAM_STATUSES = Set.of(COMPLETED, NOT_TAKEN, IN_PROGRESS);
	private static final Sort ORDER = Sort.by("createdAt", "id");

	private final ListeningLessonRepository listening;
	private final ReadingLessonRepository reading;
	private final ExamRepository exams;
	private final QuestionRepository questions;
	private final PracticeAttemptRepository attempts;

	// --- Nghe -----------------------------------------------------------------------------------

	@Transactional(readOnly = true)
	public PageResponse<ListeningSummaryResponse> listListening(UUID userId, String search, UUID topicId,
			String level, String status, int page, int pageSize) {
		Level levelFilter = parseLevel(level);
		String statusFilter = parseStatus(status, LESSON_STATUSES);
		List<ListeningLesson> found = listening.findAll(
				ContentSpecs.<ListeningLesson>learnerFilter(search, topicId, levelFilter, true), ORDER);
		List<ListeningSummaryResponse> all = summarizeListening(userId, found).stream()
				.filter(s -> matchesLesson(statusFilter, s.isCompleted()))
				.toList();
		return PageResponse.of(all, page, pageSize);
	}

	@Transactional(readOnly = true)
	public ListeningDetailResponse getListening(UUID userId, UUID id) {
		ListeningLesson lesson = listening.findByIdAndStatus(id, ContentStatus.ACTIVE)
				.orElseThrow(PracticeCatalogService::notFound);
		ListeningSummaryResponse s = summarizeListening(userId, List.of(lesson)).get(0);
		return new ListeningDetailResponse(s.id(), s.title(), s.description(), s.topicId(), s.topicName(), s.level(),
				s.durationSeconds(), s.questionCount(), s.isCompleted(), s.lastScore(), lesson.getAudioUrl(),
				lesson.getAudioUrl() == null || lesson.getAudioUrl().isBlank() ? lesson.getTranscript() : null,
				toQuestions(questions.findByListeningLessonIdOrderBySortOrder(id)));
	}

	private List<ListeningSummaryResponse> summarizeListening(UUID userId, List<ListeningLesson> found) {
		if (found.isEmpty()) {
			return List.of();
		}
		List<UUID> ids = found.stream().map(ListeningLesson::getId).toList();
		Map<UUID, Long> counts = counts(questions.countByListeningLessons(ids));
		Map<UUID, BigDecimal> scores = latest(attempts.listeningScores(userId, ids));
		return found.stream().map(l -> new ListeningSummaryResponse(l.getId(), L10n.of(l.getTitleVi(), l.getTitleEn()),
				description(l.getDescriptionVi(), l.getDescriptionEn()), l.getTopic().getId(),
				L10n.of(l.getTopic().getNameVi(), l.getTopic().getNameEn()), l.getLevel(), l.getDurationSeconds(),
				counts.getOrDefault(l.getId(), 0L), scores.containsKey(l.getId()), scores.get(l.getId()))).toList();
	}

	// --- Đọc ------------------------------------------------------------------------------------

	@Transactional(readOnly = true)
	public PageResponse<ReadingSummaryResponse> listReading(UUID userId, String search, UUID topicId, String level,
			String status, int page, int pageSize) {
		Level levelFilter = parseLevel(level);
		String statusFilter = parseStatus(status, LESSON_STATUSES);
		List<ReadingLesson> found = reading.findAll(
				ContentSpecs.<ReadingLesson>learnerFilter(search, topicId, levelFilter, true), ORDER);
		List<ReadingSummaryResponse> all = summarizeReading(userId, found).stream()
				.filter(s -> matchesLesson(statusFilter, s.isCompleted()))
				.toList();
		return PageResponse.of(all, page, pageSize);
	}

	@Transactional(readOnly = true)
	public ReadingDetailResponse getReading(UUID userId, UUID id) {
		ReadingLesson lesson = reading.findByIdAndStatus(id, ContentStatus.ACTIVE)
				.orElseThrow(PracticeCatalogService::notFound);
		ReadingSummaryResponse s = summarizeReading(userId, List.of(lesson)).get(0);
		return new ReadingDetailResponse(s.id(), s.title(), s.description(), s.topicId(), s.topicName(), s.level(),
				s.wordCount(), s.questionCount(), s.timeLimitMinutes(), s.isCompleted(), s.lastScore(),
				List.copyOf(lesson.getParagraphs()), toQuestions(questions.findByReadingLessonIdOrderBySortOrder(id)));
	}

	private List<ReadingSummaryResponse> summarizeReading(UUID userId, List<ReadingLesson> found) {
		if (found.isEmpty()) {
			return List.of();
		}
		List<UUID> ids = found.stream().map(ReadingLesson::getId).toList();
		Map<UUID, Long> counts = counts(questions.countByReadingLessons(ids));
		Map<UUID, BigDecimal> scores = latest(attempts.readingScores(userId, ids));
		return found.stream().map(l -> new ReadingSummaryResponse(l.getId(), L10n.of(l.getTitleVi(), l.getTitleEn()),
				description(l.getDescriptionVi(), l.getDescriptionEn()), l.getTopic().getId(),
				L10n.of(l.getTopic().getNameVi(), l.getTopic().getNameEn()), l.getLevel(), l.getWordCount(),
				counts.getOrDefault(l.getId(), 0L), l.getTimeLimitMinutes(), scores.containsKey(l.getId()),
				scores.get(l.getId()))).toList();
	}

	// --- Kiểm tra -------------------------------------------------------------------------------

	@Transactional(readOnly = true)
	public PageResponse<ExamSummaryResponse> listExams(UUID userId, String search, String level, String status,
			int page, int pageSize) {
		Level levelFilter = parseLevel(level);
		String statusFilter = parseStatus(status, EXAM_STATUSES);
		List<Exam> found = exams.findAll(ContentSpecs.<Exam>learnerFilter(search, null, levelFilter, false), ORDER);
		List<ExamSummaryResponse> all = summarizeExams(userId, found).stream()
				.filter(s -> statusFilter == null || statusFilter.equals(s.status()))
				.toList();
		return PageResponse.of(all, page, pageSize);
	}

	@Transactional(readOnly = true)
	public ExamDetailResponse getExam(UUID userId, UUID id) {
		Exam exam = exams.findByIdAndStatus(id, ContentStatus.ACTIVE).orElseThrow(PracticeCatalogService::notFound);
		ExamSummaryResponse s = summarizeExams(userId, List.of(exam)).get(0);
		return new ExamDetailResponse(s.id(), s.title(), s.description(), s.skills(), s.level(), s.questionCount(),
				s.timeLimitMinutes(), s.status(), s.lastScore(), toQuestions(questions.findByExamIdOrderBySortOrder(id)));
	}

	private List<ExamSummaryResponse> summarizeExams(UUID userId, List<Exam> found) {
		if (found.isEmpty()) {
			return List.of();
		}
		List<UUID> ids = found.stream().map(Exam::getId).toList();
		Map<UUID, Map<Skill, Long>> bySkill = new HashMap<>();
		for (ExamSkillCount row : questions.countByExams(ids)) {
			bySkill.computeIfAbsent(row.getParentId(), k -> new EnumMap<>(Skill.class)).put(row.getSkill(),
					row.getTotal());
		}
		Map<UUID, BigDecimal> scores = latest(attempts.examScores(userId, ids));
		return found.stream().map(e -> {
			Map<Skill, Long> skills = bySkill.getOrDefault(e.getId(), Map.of());
			long total = skills.values().stream().mapToLong(Long::longValue).sum();
			return new ExamSummaryResponse(e.getId(), L10n.of(e.getTitleVi(), e.getTitleEn()),
					description(e.getDescriptionVi(), e.getDescriptionEn()),
					skills.keySet().stream().sorted(Comparator.naturalOrder()).toList(), e.getLevel(), total,
					e.getTimeLimitMinutes(), scores.containsKey(e.getId()) ? COMPLETED : NOT_TAKEN,
					scores.get(e.getId()));
		}).toList();
	}

	// --- nội bộ ---------------------------------------------------------------------------------

	private static List<QuestionResponse> toQuestions(List<Question> found) {
		return IntStream.range(0, found.size()).mapToObj(i -> {
			Question q = found.get(i);
			List<QuestionOptionResponse> options = q.getKind() == QuestionKind.SINGLE_CHOICE
					? q.getOptions().stream().map(o -> new QuestionOptionResponse(o.getId(), o.getContent())).toList()
					: null;
			return new QuestionResponse(q.getId(), q.getKind(), i + 1, q.getContent(), options);
		}).toList();
	}

	private static Map<UUID, Long> counts(Collection<ParentCount> rows) {
		Map<UUID, Long> result = new HashMap<>();
		rows.forEach(r -> result.put(r.getParentId(), r.getTotal()));
		return result;
	}

	/** Danh sách đã xếp mới nhất trước, nên dòng đầu tiên của mỗi bài là lượt gần nhất. */
	private static Map<UUID, BigDecimal> latest(List<ParentScore> rows) {
		Map<UUID, BigDecimal> result = new HashMap<>();
		rows.forEach(r -> result.putIfAbsent(r.getParentId(), r.getScore()));
		return result;
	}

	private static L10n description(String vi, String en) {
		return L10n.of(vi == null ? "" : vi, en);
	}

	private static boolean matchesLesson(String status, boolean completed) {
		return status == null || (COMPLETED.equals(status) == completed);
	}

	private static Level parseLevel(String level) {
		if (level == null || level.isBlank()) {
			return null;
		}
		try {
			return Level.valueOf(level.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new ApiException(ErrorCode.VALIDATION);
		}
	}

	private static String parseStatus(String status, Set<String> allowed) {
		if (status == null || status.isBlank()) {
			return null;
		}
		String value = status.trim().toUpperCase(Locale.ROOT);
		if (!allowed.contains(value)) {
			throw new ApiException(ErrorCode.VALIDATION);
		}
		return value;
	}

	private static ApiException notFound() {
		return new ApiException(ErrorCode.NOT_FOUND);
	}
}
