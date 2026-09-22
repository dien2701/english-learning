package vn.enlearning.backend.audio;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/** Bản giả khi chưa có {@code OPENAI_API_KEY}: mọi đoạn đều ra cùng một MP3 im lặng ngắn, không gọi ra ngoài. */
@Component
@ConditionalOnExpression("'${app.audio.openai-api-key:}'.isBlank()")
class FakeSpeechSynthesizer implements SpeechSynthesizer {

	private static final String SILENCE = "audio/silence.mp3";

	private final byte[] silence;

	FakeSpeechSynthesizer() {
		try (InputStream in = new ClassPathResource(SILENCE).getInputStream()) {
			this.silence = in.readAllBytes();
		} catch (IOException e) {
			throw new IllegalStateException("Thiếu tài nguyên " + SILENCE, e);
		}
	}

	@Override
	public byte[] synthesize(String text, String voice) {
		return silence.clone();
	}
}
