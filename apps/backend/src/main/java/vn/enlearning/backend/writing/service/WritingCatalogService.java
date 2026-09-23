package vn.enlearning.backend.writing.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ContentSort;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.writing.dto.WritingPromptDetailResponse;
import vn.enlearning.backend.writing.dto.WritingPromptSummaryResponse;
import vn.enlearning.backend.writing.repository.AiFeedbackRepository;
import vn.enlearning.backend.writing.repository.AiFeedbackRepository.PromptScore;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository.PromptStatus;

/**
 * Danh sách và chi tiết đề Luyện viết cho người học (chỉ đề ACTIVE). {@code status} và {@code lastScore}
 * lấy từ bài nộp của chính người dùng: trạng thái bài gần nhất, điểm của bài đã chấm gần nhất.
 */
@Service
@RequiredArgsConstructor
public class WritingCatalogService {

	static final String NOT_STARTED = "NOT_STARTED";

	private final WritingPromptRepository prompts;
	private final WritingSubmissionRepository submissions;
	private final AiFeedbackRepository feedbacks;

	@Transactional(readOnly = true)
	public PageResponse<WritingPromptSummaryResponse> list(UUID userId, String search, UUID topicId, String level,
			String sort, int page, int pageSize) {
		List<WritingPrompt> found = prompts.findAll(learnerFilter(search, topicId, parseLevel(level)),
				ContentSort.resolve(sort));
		return PageResponse.of(summarize(userId, found), page, pageSize);
	}

	@Transactional(readOnly = true)
	public WritingPromptDetailResponse get(UUID userId, UUID id) {
		WritingPrompt prompt = prompts.findByIdAndStatus(id, ContentStatus.ACTIVE)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		WritingPromptSummaryResponse s = summarize(userId, List.of(prompt)).get(0);
		return new WritingPromptDetailResponse(s.id(), s.title(), s.topicId(), s.topicName(), s.level(),
				s.suggestedMinutes(), s.minWords(), s.status(), s.lastScore(), s.imageUrl(), s.imageAuthor(),
				s.imageAuthorUrl(), prompt.getInstructions(), List.copyOf(prompt.getHints()));
	}

	private List<WritingPromptSummaryResponse> summarize(UUID userId, List<WritingPrompt> found) {
		if (found.isEmpty()) {
			return List.of();
		}
		List<UUID> ids = found.stream().map(WritingPrompt::getId).toList();
		Map<UUID, String> status = new HashMap<>();
		for (PromptStatus row : submissions.promptStatuses(userId, ids)) {
			status.putIfAbsent(row.getParentId(), row.getStatus().name());
		}
		Map<UUID, BigDecimal> scores = new HashMap<>();
		for (PromptScore row : feedbacks.promptScores(userId, ids)) {
			scores.putIfAbsent(row.getParentId(), row.getScore());
		}
		return found.stream().map(p -> new WritingPromptSummaryResponse(p.getId(),
				L10n.of(p.getTitleVi(), p.getTitleEn()), p.getTopic().getId(),
				L10n.of(p.getTopic().getNameVi(), p.getTopic().getNameEn()), p.getLevel(), p.getSuggestedMinutes(),
				p.getMinWords(), status.getOrDefault(p.getId(), NOT_STARTED), scores.get(p.getId()), p.getImageUrl(),
				p.getImageAuthor(), p.getImageAuthorUrl())).toList();
	}

	private static Specification<WritingPrompt> learnerFilter(String search, UUID topicId, Level level) {
		return (root, query, cb) -> {
			root.fetch("topic", JoinType.INNER);
			List<Predicate> where = new ArrayList<>();
			where.add(cb.equal(root.get("status"), ContentStatus.ACTIVE));
			if (topicId != null) {
				where.add(cb.equal(root.get("topic").get("id"), topicId));
			}
			if (level != null) {
				where.add(cb.equal(root.get("level"), level));
			}
			if (search != null && !search.isBlank()) {
				String like = "%" + search.trim().toLowerCase(Locale.ROOT)
						.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
				where.add(cb.or(
						cb.like(cb.lower(root.get("titleVi")), like, '\\'),
						cb.like(cb.lower(root.get("titleEn")), like, '\\')));
			}
			return cb.and(where.toArray(Predicate[]::new));
		};
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
}
