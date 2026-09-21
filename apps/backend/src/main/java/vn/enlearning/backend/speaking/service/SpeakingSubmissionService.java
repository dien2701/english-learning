package vn.enlearning.backend.speaking.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.ai.AiProperties;
import vn.enlearning.backend.ai.SpeakingGrader;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.SpeakingPrompt;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.speaking.dto.SpeakingResultResponse;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;

/**
 * Nộp bài nói (một tệp âm thanh cho mỗi câu) và xem kết quả. Kiểm hợp lệ trước khi lưu; lượt được lưu (GRADING,
 * commit) rồi mới chấm nền. Âm thanh chỉ đọc vào bộ nhớ để chuyển cho AI và không bao giờ được lưu. {@link #submit}
 * cố ý KHÔNG mở giao dịch bao ngoài để lượt đã commit trước khi luồng nền chạy. Lượt của người khác là 404.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeakingSubmissionService {

	/** Khớp {@code spring.servlet.multipart} trong application.yml. */
	static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
	static final long MAX_TOTAL_BYTES = 15L * 1024 * 1024;
	static final int MAX_DURATION_SECONDS = 3600;
	static final Set<String> AUDIO_TYPES = Set.of("audio/webm", "audio/ogg", "audio/mpeg", "audio/mp3", "audio/mp4",
			"audio/x-m4a", "audio/m4a", "audio/aac", "audio/wav", "audio/x-wav", "audio/wave");

	private final SpeakingLessonRepository lessons;
	private final SpeakingAttemptRepository attempts;
	private final UserRepository users;
	private final SpeakingGradingService grading;
	private final SpeakingResultMapper mapper;
	private final AiProperties properties;
	private final Clock clock;

	/**
	 * @param promptIds câu đã đọc; {@code audio[i]} là bản ghi của {@code promptIds[i]}
	 */
	public SpeakingResultResponse submit(UUID userId, UUID lessonId, List<UUID> promptIds, List<MultipartFile> audio,
			int durationSeconds) {
		SpeakingLesson lesson = lessons.findByIdAndStatus(lessonId, ContentStatus.ACTIVE)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		List<UUID> ids = promptIds == null ? List.of() : promptIds;
		List<MultipartFile> files = audio == null ? List.of() : audio.stream().filter(f -> f != null).toList();
		if (ids.isEmpty() || files.isEmpty() || files.stream().anyMatch(MultipartFile::isEmpty)) {
			throw ApiException.field(ErrorCode.VALIDATION, "audio", "errors.noRecording");
		}
		if (ids.size() != files.size()) {
			throw ApiException.field(ErrorCode.VALIDATION, "audio", "errors.field.audioMismatch");
		}
		Map<UUID, SpeakingPrompt> byId = lesson.getPrompts().stream()
				.collect(Collectors.toMap(SpeakingPrompt::id, Function.identity(), (a, b) -> a));
		Set<UUID> seen = new HashSet<>();
		for (UUID id : ids) {
			if (!byId.containsKey(id) || !seen.add(id)) {
				throw ApiException.field(ErrorCode.VALIDATION, "promptIds", "errors.field.invalidPrompt");
			}
		}
		long total = 0;
		for (MultipartFile file : files) {
			if (!AUDIO_TYPES.contains(baseType(file.getContentType()))) {
				throw new ApiException(ErrorCode.AUDIO_UNSUPPORTED);
			}
			total += file.getSize();
			if (file.getSize() > MAX_FILE_BYTES || total > MAX_TOTAL_BYTES) {
				throw new ApiException(ErrorCode.AUDIO_TOO_LARGE);
			}
		}

		List<SpeakingGrader.PromptAudio> answers = new ArrayList<>();
		for (int i = 0; i < ids.size(); i++) {
			MultipartFile file = files.get(i);
			answers.add(new SpeakingGrader.PromptAudio(ids.get(i), byId.get(ids.get(i)).text(),
					file.getOriginalFilename(), baseType(file.getContentType()), read(file)));
		}

		int duration = Math.max(0, Math.min(durationSeconds, MAX_DURATION_SECONDS));
		Instant now = clock.instant();
		SpeakingAttempt attempt = new SpeakingAttempt();
		attempt.setUser(users.getReferenceById(userId));
		attempt.setLesson(lesson);
		attempt.setStatus(SpeakingAttemptStatus.GRADING);
		attempt.setStartedAt(now.minusSeconds(duration));
		attempt.setSubmittedAt(now);
		attempt.setDurationSeconds(duration);
		attempts.saveAndFlush(attempt);

		try {
			grading.gradeAsync(attempt.getId(),
					new SpeakingGrader.Request(lesson.getTitleVi(), List.copyOf(answers)));
		} catch (TaskRejectedException e) {
			log.warn("Hàng đợi chấm AI đầy, bài nói {} chuyển FAILED", attempt.getId());
			attempts.transition(attempt.getId(), SpeakingAttemptStatus.GRADING, SpeakingAttemptStatus.FAILED,
					clock.instant());
			attempt.setStatus(SpeakingAttemptStatus.FAILED);
		}
		return mapper.toResponse(attempt);
	}

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

	private static byte[] read(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException e) {
			throw new UncheckedIOException("Không đọc được tệp âm thanh", e);
		}
	}
}
