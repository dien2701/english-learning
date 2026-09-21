package vn.enlearning.backend.speaking.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.ai.SpeakingGrader;
import vn.enlearning.backend.ai.SpeakingScoring;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.SpeakingPrompt;
import vn.enlearning.backend.entity.SpeakingPromptFeedback;
import vn.enlearning.backend.entity.SpeakingPromptResult;
import vn.enlearning.backend.entity.SpeakingWordIssue;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.speaking.dto.SpeakingAttemptResponse;
import vn.enlearning.backend.speaking.dto.SpeakingPromptAssessmentResponse;
import vn.enlearning.backend.speaking.dto.SpeakingResultResponse;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;
import vn.enlearning.backend.speaking.repository.SpeakingPromptResultRepository;

/**
 * Luyện nói chấm từng câu: mở lượt {@code IN_PROGRESS}, chấm ĐỒNG BỘ mỗi bản thu ({@link #assess}) rồi nộp cuối
 * tổng hợp thành GRADED ({@link #submit}). Các hàm cố ý KHÔNG mở giao dịch bao ngoài để không giữ kết nối DB trong
 * lúc gọi AI (chậm); chỉ bước ghi kết quả chạy trong giao dịch ngắn. Âm thanh chỉ nằm trong bộ nhớ và bị bỏ sau
 * lần gọi. AI lỗi thì 503, không lưu gì, người học thu lại hoặc nộp lại. Lượt của người khác là 404.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeakingAttemptService {

	private static final BigDecimal MAX_SCORE = BigDecimal.TEN;

	private final SpeakingLessonRepository lessons;
	private final SpeakingAttemptRepository attempts;
	private final SpeakingPromptResultRepository results;
	private final UserRepository users;
	private final SpeakingGrader grader;
	private final SpeakingResultMapper mapper;
	private final TransactionTemplate tx;
	private final Clock clock;

	/** Tạo lượt IN_PROGRESS, hoặc dùng lại lượt IN_PROGRESS của chính người đó ở bài này. */
	public SpeakingAttemptResponse start(UUID userId, UUID lessonId) {
		SpeakingLesson lesson = lessons.findByIdAndStatus(lessonId, ContentStatus.ACTIVE)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		return tx.execute(status -> {
			SpeakingAttempt attempt = attempts
					.findFirstByUserIdAndLessonIdAndStatusOrderByCreatedAtDescIdDesc(userId, lessonId,
							SpeakingAttemptStatus.IN_PROGRESS)
					.orElse(null);
			if (attempt == null) {
				Instant now = clock.instant();
				attempt = new SpeakingAttempt();
				attempt.setUser(users.getReferenceById(userId));
				attempt.setLesson(lesson);
				attempt.setStatus(SpeakingAttemptStatus.IN_PROGRESS);
				attempt.setStartedAt(now);
				attempt.setSubmittedAt(now);
				attempts.saveAndFlush(attempt);
			}
			List<SpeakingPromptAssessmentResponse> done = results
					.findByAttemptIdOrderByCreatedAtAscIdAsc(attempt.getId()).stream()
					.map(SpeakingAttemptService::toResponse).toList();
			return new SpeakingAttemptResponse(attempt.getId(), lessonId, attempt.getStatus(), done);
		});
	}

	/** Chấm một câu và lưu (ghi đè nếu thu lại). */
	public SpeakingPromptAssessmentResponse assess(UUID userId, UUID attemptId, UUID promptId, MultipartFile audio) {
		SpeakingAttempt attempt = findInProgress(userId, attemptId);
		SpeakingPrompt prompt = attempt.getLesson().getPrompts().stream().filter(p -> p.id().equals(promptId))
				.findFirst()
				.orElseThrow(() -> ApiException.field(ErrorCode.VALIDATION, "promptId", "errors.field.invalidPrompt"));
		validateAudio(audio);

		SpeakingGrader.PromptAssessment assessment;
		try {
			assessment = grader.assessPrompt(prompt.text(), audio.getOriginalFilename(),
					SpeakingSubmissionService.baseType(audio.getContentType()), read(audio));
			validate(assessment);
		} catch (RuntimeException e) {
			log.warn("Chấm câu {} của lượt nói {} thất bại: {}", promptId, attemptId, e.toString());
			throw new ApiException(ErrorCode.AI_UNAVAILABLE);
		}

		try {
			return tx.execute(status -> {
				SpeakingAttempt fresh = attempts.findByIdAndUserId(attemptId, userId)
						.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
				if (fresh.getStatus() != SpeakingAttemptStatus.IN_PROGRESS) {
					throw new ApiException(ErrorCode.INVALID_STATE);
				}
				SpeakingPromptResult result = results.findByAttemptIdAndPromptId(attemptId, promptId)
						.orElseGet(() -> {
							SpeakingPromptResult created = new SpeakingPromptResult();
							created.setAttempt(fresh);
							created.setPromptId(promptId);
							return created;
						});
				result.setTranscript(assessment.transcript());
				result.setScore(assessment.score());
				result.setWordIssues(new ArrayList<>(assessment.wordIssues()));
				result.setTips(new ArrayList<>(assessment.tips()));
				result.setModelName(assessment.modelName());
				results.saveAndFlush(result);
				SpeakingPromptAssessmentResponse response = toResponse(result);
				attempts.touch(attemptId, clock.instant());
				return response;
			});
		} catch (DataIntegrityViolationException e) {
			// Hai lần chấm cùng một câu chạy đồng thời (bấm đúp): lần đến sau bị unique(attempt_id, prompt_id) chặn.
			throw new ApiException(ErrorCode.INVALID_STATE);
		}
	}

	/** Tổng hợp: cần đủ mọi câu đã có kết quả; điểm từ kết quả từng câu, {@code improvements} từ một lời gọi AI văn bản. */
	public SpeakingResultResponse submit(UUID userId, UUID attemptId, Integer durationSeconds) {
		SpeakingAttempt attempt = findInProgress(userId, attemptId);
		SpeakingLesson lesson = attempt.getLesson();
		Map<UUID, SpeakingPromptResult> byPrompt = new HashMap<>();
		for (SpeakingPromptResult r : results.findByAttemptIdOrderByCreatedAtAscIdAsc(attemptId)) {
			byPrompt.put(r.getPromptId(), r);
		}
		List<SpeakingGrader.PromptSummary> summaries = new ArrayList<>();
		List<SpeakingPromptFeedback> feedback = new ArrayList<>();
		String modelName = null;
		for (SpeakingPrompt prompt : lesson.getPrompts()) {
			SpeakingPromptResult r = byPrompt.get(prompt.id());
			if (r == null) {
				throw new ApiException(ErrorCode.SPEAKING_INCOMPLETE);
			}
			summaries.add(new SpeakingGrader.PromptSummary(prompt.text(), new SpeakingGrader.PromptAssessment(
					r.getTranscript(), r.getScore(), r.getWordIssues(), r.getTips(), r.getModelName())));
			feedback.add(new SpeakingPromptFeedback(prompt.id(), r.getTranscript(), r.getScore(),
					r.getWordIssues().stream().map(SpeakingWordIssue::word).toList(), String.join(" ", r.getTips())));
			modelName = r.getModelName();
		}
		if (summaries.isEmpty()) {
			throw new ApiException(ErrorCode.SPEAKING_INCOMPLETE);
		}

		List<String> improvements;
		try {
			improvements = grader.writeImprovements(lesson.getTitleVi(), List.copyOf(summaries));
			if (improvements == null || improvements.isEmpty()) {
				throw new IllegalStateException("AI không trả điểm cần cải thiện");
			}
		} catch (RuntimeException e) {
			log.warn("Viết nhận xét tổng hợp cho lượt nói {} thất bại: {}", attemptId, e.toString());
			throw new ApiException(ErrorCode.AI_UNAVAILABLE);
		}
		SpeakingScoring.Scores scores = SpeakingScoring.aggregate(summaries);
		int duration = Math.max(0,
				Math.min(durationSeconds == null ? 0 : durationSeconds, SpeakingSubmissionService.MAX_DURATION_SECONDS));
		String model = modelName;

		return tx.execute(status -> {
			SpeakingAttempt fresh = attempts.findByIdAndUserId(attemptId, userId)
					.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
			if (fresh.getStatus() != SpeakingAttemptStatus.IN_PROGRESS) {
				throw new ApiException(ErrorCode.INVALID_STATE);
			}
			Instant now = clock.instant();
			fresh.setOverallScore(scores.overall());
			fresh.setPronunciationScore(scores.pronunciation());
			fresh.setVocabularyScore(scores.vocabulary());
			fresh.setGrammarScore(scores.grammar());
			fresh.setFluencyScore(scores.fluency());
			fresh.setRelevanceScore(scores.relevance());
			fresh.setImprovements(new ArrayList<>(improvements));
			fresh.setPromptFeedback(new ArrayList<>(feedback));
			fresh.setModelName(model);
			fresh.setDurationSeconds(duration);
			fresh.setSubmittedAt(now.isBefore(fresh.getStartedAt()) ? fresh.getStartedAt() : now);
			fresh.setStatus(SpeakingAttemptStatus.GRADED);
			attempts.saveAndFlush(fresh);
			return mapper.toResponse(fresh);
		});
	}

	private SpeakingAttempt findInProgress(UUID userId, UUID attemptId) {
		SpeakingAttempt attempt = attempts.findByIdAndUserId(attemptId, userId)
				.orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
		if (attempt.getStatus() != SpeakingAttemptStatus.IN_PROGRESS) {
			throw new ApiException(ErrorCode.INVALID_STATE);
		}
		return attempt;
	}

	/** Cùng giới hạn định dạng và kích thước với luồng nộp một lần. */
	private static void validateAudio(MultipartFile audio) {
		if (audio == null || audio.isEmpty()) {
			throw ApiException.field(ErrorCode.VALIDATION, "audio", "errors.noRecording");
		}
		if (!SpeakingSubmissionService.AUDIO_TYPES.contains(SpeakingSubmissionService.baseType(audio.getContentType()))) {
			throw new ApiException(ErrorCode.AUDIO_UNSUPPORTED);
		}
		if (audio.getSize() > SpeakingSubmissionService.MAX_FILE_BYTES) {
			throw new ApiException(ErrorCode.AUDIO_TOO_LARGE);
		}
	}

	/** Điểm ngoài thang 10 bị CHECK của DB từ chối, nên chặn sớm. */
	private static void validate(SpeakingGrader.PromptAssessment a) {
		if (a == null || a.transcript() == null || a.wordIssues() == null || a.tips() == null || a.score() == null
				|| a.score().signum() < 0 || a.score().compareTo(MAX_SCORE) > 0) {
			throw new IllegalStateException("Kết quả chấm câu không hợp lệ");
		}
	}

	private static SpeakingPromptAssessmentResponse toResponse(SpeakingPromptResult r) {
		return new SpeakingPromptAssessmentResponse(r.getPromptId(), r.getTranscript(), r.getScore(),
				List.copyOf(r.getWordIssues()), List.copyOf(r.getTips()));
	}

	private static byte[] read(MultipartFile file) {
		try {
			return file.getBytes();
		} catch (IOException e) {
			throw new UncheckedIOException("Không đọc được tệp âm thanh", e);
		}
	}
}
