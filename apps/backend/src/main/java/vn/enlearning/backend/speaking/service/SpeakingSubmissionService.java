package vn.enlearning.backend.speaking.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.ai.AiProperties;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.speaking.dto.SpeakingResultResponse;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;

/**
 * Xem kết quả một lượt nói, và giữ các giới hạn âm thanh dùng chung cho luồng chấm từng câu
 * ({@link SpeakingAttemptService}). Lượt của người khác là 404.
 */
@Service
@RequiredArgsConstructor
public class SpeakingSubmissionService {

	/** Khớp {@code spring.servlet.multipart} trong application.yml. */
	static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
	static final int MAX_DURATION_SECONDS = 3600;
	static final Set<String> AUDIO_TYPES = Set.of("audio/webm", "audio/ogg", "audio/mpeg", "audio/mp3", "audio/mp4",
			"audio/x-m4a", "audio/m4a", "audio/aac", "audio/wav", "audio/x-wav", "audio/wave");

	private final SpeakingAttemptRepository attempts;
	private final SpeakingResultMapper mapper;
	private final AiProperties properties;
	private final Clock clock;

	/** Hỏi lại kết quả. Lượt GRADING quá hạn (AI treo hoặc server đã khởi động lại) thì chuyển FAILED. */
	@Transactional
	public SpeakingResultResponse getResult(UUID userId, UUID attemptId) {
		SpeakingAttempt attempt = find(userId, attemptId);
		if (attempt.getStatus() == SpeakingAttemptStatus.GRADING && isStale(attempt)) {
			attempts.transition(attemptId, SpeakingAttemptStatus.GRADING, SpeakingAttemptStatus.FAILED,
					clock.instant());
			attempt = find(userId, attemptId);
		}
		return mapper.toResponse(attempt);
	}

	private SpeakingAttempt find(UUID userId, UUID attemptId) {
		return attempts.findByIdAndUserId(attemptId, userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	private boolean isStale(SpeakingAttempt attempt) {
		Instant deadline = clock.instant().minus(properties.gradingTimeout());
		return attempt.getUpdatedAt() != null && attempt.getUpdatedAt().isBefore(deadline);
	}

	/** Bỏ tham số như {@code ;codecs=opus}; rỗng nếu không có kiểu nội dung. */
	static String baseType(String contentType) {
		if (contentType == null) {
			return "";
		}
		int semicolon = contentType.indexOf(';');
		return (semicolon < 0 ? contentType : contentType.substring(0, semicolon)).trim().toLowerCase(Locale.ROOT);
	}
}
