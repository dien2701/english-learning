package vn.enlearning.backend.ai;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.entity.SpeakingPromptFeedback;

/**
 * Bản giả: transcript cố định (đúng câu đề bài), điểm cố định, có độ trễ mô phỏng. Tệp âm thanh có tên chứa
 * {@link #FAIL_MARKER} (ví dụ {@code fail.webm}) làm bản giả lỗi, để thử trạng thái FAILED.
 */
@Component
@Conditional(OpenAiKeyMissing.class)
@RequiredArgsConstructor
class FakeSpeakingGrader implements SpeakingGrader {

	static final String FAIL_MARKER = "fail";
	static final String MODEL_NAME = "fake-speaking-grader";
	private static final int LONG_WORD = 7;

	private final AiProperties properties;

	@Override
	public Grade grade(Request request) {
		sleep();
		for (PromptAudio answer : request.answers()) {
			if (answer.filename() != null && answer.filename().toLowerCase(Locale.ROOT).contains(FAIL_MARKER)) {
				throw new AiGradingException("Bản giả bị ép lỗi bằng tên tệp chứa \"" + FAIL_MARKER + "\"");
			}
		}
		List<SpeakingPromptFeedback> feedback = new ArrayList<>();
		for (PromptAudio answer : request.answers()) {
			feedback.add(new SpeakingPromptFeedback(answer.promptId(), answer.promptText(), BigDecimal.valueOf(7.5),
					longWords(answer.promptText()),
					"Đọc rõ ràng, tốc độ ổn. (Nhận xét mẫu từ bản giả, không phải chấm thật.)"));
		}
		return new Grade(BigDecimal.valueOf(7.6), BigDecimal.valueOf(7.0), BigDecimal.valueOf(7.5),
				BigDecimal.valueOf(8.0), BigDecimal.valueOf(7.0), BigDecimal.valueOf(8.5),
				List.of("Nhấn trọng âm rõ hơn ở các từ dài.", "Giữ nhịp đọc đều, tránh ngắt giữa cụm từ."),
				feedback, MODEL_NAME);
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
