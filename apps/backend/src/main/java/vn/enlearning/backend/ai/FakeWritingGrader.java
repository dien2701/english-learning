package vn.enlearning.backend.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.entity.AiFeedbackIssue;
import vn.enlearning.backend.entity.enums.IssueCategory;

/**
 * Bản giả: trả kết quả cố định theo độ dài bài, có độ trễ mô phỏng. Để thử luồng lỗi:
 * bài chứa {@link #FAIL_ONCE_MARKER} lỗi ở lần chấm đầu và thành công khi chấm lại (cùng nội dung);
 * bài chứa {@link #FAIL_ALWAYS_MARKER} luôn lỗi.
 */
@Component
@Conditional(OpenAiKeyMissing.class)
@RequiredArgsConstructor
class FakeWritingGrader implements WritingGrader {

	static final String FAIL_ONCE_MARKER = "[fail]";
	static final String FAIL_ALWAYS_MARKER = "[fail-always]";
	static final String MODEL_NAME = "fake-writing-grader";
	private static final int MAX_REMEMBERED = 1000;

	private final AiProperties properties;
	private final Set<String> failedOnce = ConcurrentHashMap.newKeySet();

	@Override
	public Grade grade(Request request) {
		sleep();
		String lower = request.content().toLowerCase(Locale.ROOT);
		if (lower.contains(FAIL_ALWAYS_MARKER)) {
			throw new AiGradingException("Bản giả bị ép lỗi bằng " + FAIL_ALWAYS_MARKER);
		}
		if (lower.contains(FAIL_ONCE_MARKER) && shouldFailOnce(lower)) {
			throw new AiGradingException("Bản giả bị ép lỗi bằng " + FAIL_ONCE_MARKER);
		}
		double ratio = Math.min(1.0, request.wordCount() / (double) Math.max(1, request.minWords()));
		BigDecimal grammar = score(6.0 + 2.0 * ratio);
		BigDecimal vocabulary = score(5.5 + 2.5 * ratio);
		BigDecimal expression = score(5.0 + 3.0 * ratio);
		BigDecimal overall = grammar.add(vocabulary).add(expression)
				.divide(BigDecimal.valueOf(3), 1, RoundingMode.HALF_UP);
		String excerpt = firstSentence(request.content());
		return new Grade(overall, grammar, vocabulary, expression,
				"Bài viết rõ ràng và đủ ý. (Nhận xét mẫu từ bản giả, không phải chấm thật.)",
				List.of(
						new AiFeedbackIssue(IssueCategory.GRAMMAR, excerpt,
								"Kiểm tra sự hoà hợp giữa chủ ngữ và động từ.",
								"Rewrite this sentence with a clear subject and matching verb."),
						new AiFeedbackIssue(IssueCategory.VOCABULARY, excerpt,
								"Từ được dùng khá chung chung.",
								"Replace general words with more precise ones."),
						new AiFeedbackIssue(IssueCategory.EXPRESSION, excerpt,
								"Câu có thể tự nhiên hơn.",
								"Vary sentence openings and use linking words.")),
				MODEL_NAME);
	}

	/** true ở lần đầu thấy nội dung này; các lần sau (chấm lại) false. */
	private boolean shouldFailOnce(String content) {
		if (failedOnce.size() >= MAX_REMEMBERED) {
			failedOnce.clear();
		}
		return failedOnce.add(content);
	}

	private void sleep() {
		long millis = properties.fakeLatency().toMillis();
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AiGradingException("Bị ngắt khi đang chấm", e);
		}
	}

	private static BigDecimal score(double value) {
		return BigDecimal.valueOf(Math.min(10.0, value)).setScale(1, RoundingMode.HALF_UP);
	}

	private static String firstSentence(String content) {
		String text = content.strip();
		int end = text.length();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '.' || c == '!' || c == '?') {
				end = i + 1;
				break;
			}
		}
		return text.substring(0, Math.min(end, 80));
	}
}
