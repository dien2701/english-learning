package vn.enlearning.backend.writing.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.entity.AiFeedback;
import vn.enlearning.backend.entity.WritingSubmission;
import vn.enlearning.backend.entity.enums.SubmissionStatus;
import vn.enlearning.backend.writing.dto.WritingFeedbackResponse;
import vn.enlearning.backend.writing.dto.WritingIssueResponse;
import vn.enlearning.backend.writing.dto.WritingSubmissionResponse;
import vn.enlearning.backend.writing.repository.AiFeedbackRepository;

/** Đổi bài nộp thành phản hồi; chỉ bài GRADED mới có {@code feedback}. Cần {@code prompt} đã nạp sẵn. */
@Component
@RequiredArgsConstructor
class WritingMapper {

	private final AiFeedbackRepository feedbacks;

	WritingSubmissionResponse toResponse(WritingSubmission submission) {
		return toResponses(List.of(submission)).get(0);
	}

	List<WritingSubmissionResponse> toResponses(List<WritingSubmission> found) {
		List<UUID> graded = found.stream().filter(s -> s.getStatus() == SubmissionStatus.GRADED)
				.map(WritingSubmission::getId).toList();
		Map<UUID, AiFeedback> bySubmission = new HashMap<>();
		if (!graded.isEmpty()) {
			feedbacks.findBySubmissionIdIn(graded).forEach(f -> bySubmission.put(f.getSubmission().getId(), f));
		}
		return found.stream().map(s -> new WritingSubmissionResponse(s.getId(), s.getPrompt().getId(),
				L10n.of(s.getPrompt().getTitleVi(), s.getPrompt().getTitleEn()), s.getContent(), s.getWordCount(),
				s.getStatus(), s.getSubmittedAt(), feedback(bySubmission.get(s.getId())))).toList();
	}

	private static WritingFeedbackResponse feedback(AiFeedback f) {
		if (f == null) {
			return null;
		}
		List<WritingIssueResponse> issues = IntStream.range(0, f.getIssues().size()).mapToObj(i -> {
			var issue = f.getIssues().get(i);
			return new WritingIssueResponse("i" + (i + 1), issue.category(), issue.excerpt(), issue.problem(),
					issue.suggestion());
		}).toList();
		return new WritingFeedbackResponse(f.getOverallScore(), f.getGrammarScore(), f.getVocabularyScore(),
				f.getExpressionScore(), f.getSummary(), issues);
	}
}
