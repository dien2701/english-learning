package vn.enlearning.backend.ai;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.entity.SpeakingWordIssue;

/**
 * Bản giả: transcript cố định (đúng câu đề bài), điểm cố định, có độ trễ mô phỏng. Tệp âm thanh có tên chứa
 * {@link #FAIL_MARKER} (ví dụ {@code fail.webm}) làm bản giả lỗi, để thử trạng thái FAILED.
 */
@Component
@Conditional(GeminiKeyMissing.class)
@RequiredArgsConstructor
class FakeSpeakingGrader implements SpeakingGrader {

	static final String FAIL_MARKER = "fail";
	static final String MODEL_NAME = "fake-speaking-grader";
	private static final int LONG_WORD = 7;

	private final AiProperties properties;

	@Override
	public PromptAssessment assessPrompt(String promptText, String filename, String contentType, byte[] audio) {
		sleep();
		if (filename != null && filename.toLowerCase(Locale.ROOT).contains(FAIL_MARKER)) {
			throw new AiGradingException("Bản giả bị ép lỗi bằng tên tệp chứa \"" + FAIL_MARKER + "\"");
		}
		List<SpeakingWordIssue> issues = longWords(promptText).stream()
				.map(word -> new SpeakingWordIssue(word, word.substring(0, word.length() - 1),
						"Thiếu âm cuối (nhận xét mẫu từ bản giả)", "Đọc rõ âm cuối của từ \"" + word + "\"."))
				.toList();
		return new PromptAssessment(promptText, BigDecimal.valueOf(7.5), issues,
				List.of("Đọc rõ ràng, tốc độ ổn. (Nhận xét mẫu từ bản giả, không phải chấm thật.)"), MODEL_NAME);
	}

	@Override
	public List<String> writeImprovements(String lessonTitle, List<PromptSummary> prompts) {
		sleep();
		return List.of("Nhấn trọng âm rõ hơn ở các từ dài.", "Giữ nhịp đọc đều, tránh ngắt giữa cụm từ.");
	}

	/** Tối đa một từ dài đầu tiên, để bản giả có dữ liệu "phát âm chưa chuẩn" mà vẫn cố định. */
	private static List<String> longWords(String text) {
		for (String word : text.split("[^\\p{L}']+")) {
			if (word.length() >= LONG_WORD) {
				return List.of(word);
			}
		}
		return List.of();
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
}
