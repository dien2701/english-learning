package vn.enlearning.backend.writing.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.ai.AiProperties;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.WritingSubmission;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.SubmissionStatus;
import vn.enlearning.backend.writing.dto.WritingSubmissionResponse;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository;

/**
 * Nộp bài, xem, lịch sử và chấm lại. Luôn lưu bài trước rồi mới gọi AI: {@link #submit} và {@link #regrade}
 * cố ý KHÔNG mở giao dịch bao ngoài, để bài đã commit trước khi luồng nền đọc nó. Bài của người khác là 404.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WritingSubmissionService {

	private static final Sort HISTORY_ORDER = Sort.by(Sort.Direction.DESC, "submittedAt", "id");

	private final WritingPromptRepository prompts;
	private final WritingSubmissionRepository submissions;
	private final UserRepository users;
	private final WritingGradingService grading;
	private final WritingMapper mapper;
	private final AiProperties properties;
	private final Clock clock;

	public WritingSubmissionResponse submit(UUID userId, UUID promptId, String content) {
		WritingPrompt prompt = prompts.findByIdAndStatus(promptId, ContentStatus.ACTIVE)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		String text = content.strip();
		int words = countWords(text);
		if (words < prompt.getMinWords()) {
			throw ApiException.field(ErrorCode.VALIDATION, "content", "errors.field.essayTooShort");
		}
		WritingSubmission submission = new WritingSubmission();
		submission.setUser(users.getReferenceById(userId));
		submission.setPrompt(prompt);
		submission.setContent(text);
		submission.setWordCount(words);
		submission.setStatus(SubmissionStatus.GRADING);
		submission.setSubmittedAt(clock.instant());
		submissions.saveAndFlush(submission);

		if (!dispatch(submission.getId())) {
			submissions.transition(submission.getId(), SubmissionStatus.GRADING, SubmissionStatus.NEEDS_RETRY,
					clock.instant());
			submission.setStatus(SubmissionStatus.NEEDS_RETRY);
		}
		return mapper.toResponse(submission);
	}

	/** Hỏi lại trạng thái. Bài GRADING quá hạn thì chuyển NEEDS_RETRY (AI treo hoặc server đã khởi động lại). */
	@Transactional
	public WritingSubmissionResponse get(UUID userId, UUID id) {
		WritingSubmission submission = find(userId, id);
		if (submission.getStatus() == SubmissionStatus.GRADING && isStale(submission)) {
			submissions.transition(id, SubmissionStatus.GRADING, SubmissionStatus.NEEDS_RETRY, clock.instant());
			submission = find(userId, id);
		}
		return mapper.toResponse(submission);
	}

	@Transactional(readOnly = true)
	public PageResponse<WritingSubmissionResponse> history(UUID userId, int page, int pageSize) {
		int size = Math.max(1, Math.min(pageSize, 100));
		int current = Math.max(1, page);
		Page<WritingSubmission> found = submissions.findByUserIdAndSubmittedAtIsNotNull(userId,
				PageRequest.of(current - 1, size, HISTORY_ORDER));
		return new PageResponse<>(mapper.toResponses(found.getContent()), current, size, found.getTotalElements(),
				found.getTotalPages());
	}

	/** Chỉ bài NEEDS_RETRY mới chấm lại được; chuyển trạng thái nguyên tử nên bấm hai lần chỉ chạy một lần. */
	public WritingSubmissionResponse regrade(UUID userId, UUID id) {
		find(userId, id);
		int moved = submissions.transition(id, SubmissionStatus.NEEDS_RETRY, SubmissionStatus.GRADING,
				clock.instant());
		if (moved == 0) {
			throw new ApiException(ErrorCode.INVALID_STATE);
		}
		if (!dispatch(id)) {
			submissions.transition(id, SubmissionStatus.GRADING, SubmissionStatus.NEEDS_RETRY, clock.instant());
		}
		return mapper.toResponse(find(userId, id));
	}

	private WritingSubmission find(UUID userId, UUID id) {
		return submissions.findByIdAndUserId(id, userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	private boolean isStale(WritingSubmission submission) {
		Instant deadline = clock.instant().minus(properties.gradingTimeout());
		return submission.getUpdatedAt() != null && submission.getUpdatedAt().isBefore(deadline);
	}

	/** false khi hàng đợi chấm đầy: người gọi chuyển bài sang NEEDS_RETRY thay vì để treo. */
	private boolean dispatch(UUID submissionId) {
		try {
			grading.gradeAsync(submissionId);
			return true;
		} catch (TaskRejectedException e) {
			log.warn("Hàng đợi chấm AI đầy, bài viết {} chuyển NEEDS_RETRY", submissionId);
			return false;
		}
	}

	static int countWords(String text) {
		String trimmed = text.strip();
		return trimmed.isEmpty() ? 0 : trimmed.split("\\s+").length;
	}
}
