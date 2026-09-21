package vn.enlearning.backend.practice.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.practice.dto.AttemptHistoryItemResponse;
import vn.enlearning.backend.practice.dto.PracticeResultResponse;
import vn.enlearning.backend.practice.dto.SubmitRequest;

/** Nộp bài (trả kết quả kèm đáp án), xem lại một lượt và lịch sử làm bài. Lượt của người khác là 404. */
@Service
@RequiredArgsConstructor
public class PracticeResultService {

	/** Kỹ năng của frontend nhưng không thuộc enum {@link Skill}; lượt nói lưu riêng nên lịch sử này luôn rỗng. */
	private static final String SPEAKING = "SPEAKING";

	private final PracticeAttemptService submissions;
	private final PracticeAttemptRepository attempts;
	private final AttemptResultMapper mapper;

	@Transactional
	public PracticeResultResponse submitListening(UUID userId, UUID lessonId, SubmitRequest request) {
		return mapper.toResult(submissions.submitListening(userId, lessonId, request).attempt());
	}

	@Transactional
	public PracticeResultResponse submitReading(UUID userId, UUID lessonId, SubmitRequest request) {
		return mapper.toResult(submissions.submitReading(userId, lessonId, request).attempt());
	}

	@Transactional
	public PracticeResultResponse submitExam(UUID userId, UUID examId, SubmitRequest request) {
		return mapper.toResult(submissions.submitExam(userId, examId, request).attempt());
	}

	@Transactional(readOnly = true)
	public PracticeResultResponse getResult(UUID userId, UUID attemptId) {
		PracticeAttempt attempt = attempts.findByIdAndUserId(attemptId, userId)
				.filter(a -> a.getStatus() == AttemptStatus.COMPLETED)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		return mapper.toResult(attempt);
	}

	/** {@code skill} là LISTENING, READING hoặc EXAM; kỹ năng khác không có lượt làm bài nên trả trang rỗng. */
	@Transactional(readOnly = true)
	public PageResponse<AttemptHistoryItemResponse> history(UUID userId, String skill, int page, int pageSize) {
		boolean speaking = skill != null && SPEAKING.equalsIgnoreCase(skill.trim());
		Skill filter = speaking ? null : parseSkill(skill);
		int size = Math.max(1, Math.min(pageSize, 100));
		int current = Math.max(1, page);
		if (speaking || (filter != null && filter != Skill.LISTENING && filter != Skill.READING
				&& filter != Skill.EXAM)) {
			return new PageResponse<>(List.of(), current, size, 0, 0);
		}
		Pageable pageable = PageRequest.of(current - 1, size,
				Sort.by(Sort.Order.desc("submittedAt"), Sort.Order.desc("id")));
		AttemptStatus done = AttemptStatus.COMPLETED;
		Page<PracticeAttempt> found;
		if (filter == null) {
			found = attempts.findByUserIdAndStatus(userId, done, pageable);
		} else {
			found = switch (filter) {
				case LISTENING -> attempts.findByUserIdAndStatusAndListeningLessonIsNotNull(userId, done, pageable);
				case READING -> attempts.findByUserIdAndStatusAndReadingLessonIsNotNull(userId, done, pageable);
				default -> attempts.findByUserIdAndStatusAndExamIsNotNull(userId, done, pageable);
			};
		}
		return new PageResponse<>(found.getContent().stream().map(mapper::toHistoryItem).toList(), current, size,
				found.getTotalElements(), found.getTotalPages());
	}

	private static Skill parseSkill(String skill) {
		if (skill == null || skill.isBlank()) {
			return null;
		}
		try {
			return Skill.valueOf(skill.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new ApiException(ErrorCode.VALIDATION);
		}
	}
}
