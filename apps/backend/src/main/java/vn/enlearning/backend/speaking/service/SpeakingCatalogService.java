package vn.enlearning.backend.speaking.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ContentSort;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.SpeakingPrompt;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.practice.service.ContentSpecs;
import vn.enlearning.backend.speaking.dto.SpeakingDetailResponse;
import vn.enlearning.backend.speaking.dto.SpeakingPromptResponse;
import vn.enlearning.backend.speaking.dto.SpeakingSummaryResponse;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository.LessonScore;

/** Danh sách và chi tiết bài Luyện nói cho người học (chỉ bài ACTIVE). */
@Service
@RequiredArgsConstructor
public class SpeakingCatalogService {

	private static final String COMPLETED = "COMPLETED";
	private static final String NOT_COMPLETED = "NOT_COMPLETED";
	private static final Set<String> STATUSES = Set.of(COMPLETED, NOT_COMPLETED);

	private final SpeakingLessonRepository lessons;
	private final SpeakingAttemptRepository attempts;

	@Transactional(readOnly = true)
	public PageResponse<SpeakingSummaryResponse> list(UUID userId, String search, UUID topicId, String level,
			String status, String sort, int page, int pageSize) {
		Level levelFilter = parseLevel(level);
		String statusFilter = parseStatus(status);
		List<SpeakingLesson> found = lessons.findAll(
				ContentSpecs.<SpeakingLesson>learnerFilter(search, topicId, levelFilter, true),
				ContentSort.resolve(sort));
		List<SpeakingSummaryResponse> all = summarize(userId, found).stream()
				.filter(s -> statusFilter == null || COMPLETED.equals(statusFilter) == s.isCompleted())
				.toList();
		return PageResponse.of(all, page, pageSize);
	}

	@Transactional(readOnly = true)
	public SpeakingDetailResponse get(UUID userId, UUID id) {
		SpeakingLesson lesson = lessons.findByIdAndStatus(id, ContentStatus.ACTIVE)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		SpeakingSummaryResponse s = summarize(userId, List.of(lesson)).get(0);
		List<SpeakingPrompt> prompts = lesson.getPrompts();
		List<SpeakingPromptResponse> items = IntStream.range(0, prompts.size()).mapToObj(i -> {
			SpeakingPrompt p = prompts.get(i);
			return new SpeakingPromptResponse(p.id(), i + 1, p.text(), p.phonetic(), p.meaningVi());
		}).toList();
		return new SpeakingDetailResponse(s.id(), s.title(), s.description(), s.topicId(), s.topicName(), s.level(),
				s.promptCount(), s.isCompleted(), s.lastScore(), s.imageUrl(), s.imageAuthor(), s.imageAuthorUrl(),
				items);
	}

	private List<SpeakingSummaryResponse> summarize(UUID userId, List<SpeakingLesson> found) {
		if (found.isEmpty()) {
			return List.of();
		}
		List<UUID> ids = found.stream().map(SpeakingLesson::getId).toList();
		Map<UUID, BigDecimal> scores = new HashMap<>();
		for (LessonScore row : attempts.gradedScores(userId, ids)) {
			scores.putIfAbsent(row.getParentId(), row.getScore());
		}
		return found.stream().map(l -> new SpeakingSummaryResponse(l.getId(), L10n.of(l.getTitleVi(), l.getTitleEn()),
				L10n.of(l.getDescriptionVi() == null ? "" : l.getDescriptionVi(), l.getDescriptionEn()),
				l.getTopic().getId(), L10n.of(l.getTopic().getNameVi(), l.getTopic().getNameEn()), l.getLevel(),
				l.getPrompts().size(), scores.containsKey(l.getId()), scores.get(l.getId()), l.getImageUrl(),
				l.getImageAuthor(), l.getImageAuthorUrl())).toList();
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

	private static String parseStatus(String status) {
		if (status == null || status.isBlank()) {
			return null;
		}
		String value = status.trim().toUpperCase(Locale.ROOT);
		if (!STATUSES.contains(value)) {
			throw new ApiException(ErrorCode.VALIDATION);
		}
		return value;
	}
}
