package vn.enlearning.backend.audio;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

/**
 * Gọi {@code POST /v1/audio/speech} của OpenAI, xin định dạng {@code mp3}. Chỉ nạp khi có
 * {@code app.audio.openai-api-key}. Log không in key hay nội dung gửi. Gặp HTTP 429 (rate limit) thì
 * chờ rồi thử lại tối đa {@link #MAX_RETRIES} lần trước khi báo lỗi.
 */
@Slf4j
@Component
@ConditionalOnExpression("!'${app.audio.openai-api-key:}'.isBlank()")
class OpenAiSpeechSynthesizer implements SpeechSynthesizer {

	private static final int MAX_RETRIES = 4;
	private static final long RETRY_DELAY_MS = 5_000;

	private final AudioProperties properties;
	private final RestClient http;

	OpenAiSpeechSynthesizer(AudioProperties properties) {
		this.properties = properties;
		this.http = AudioHttp.client(properties.openaiBaseUrl(), properties.openaiTimeout());
	}

	@Override
	public byte[] synthesize(String text, String voice) {
		for (int attempt = 1; ; attempt++) {
			try {
				return call(text, voice);
			} catch (SpeechSynthesisException e) {
				if (!isRateLimited(e) || attempt >= MAX_RETRIES) {
					throw e;
				}
				long delay = RETRY_DELAY_MS * attempt;
				log.warn("OpenAI TTS bị rate limit, chờ {}ms rồi thử lại (lần {}/{}).", delay, attempt, MAX_RETRIES);
				sleep(delay);
			}
		}
	}

	private byte[] call(String text, String voice) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("model", properties.openaiTtsModel());
		payload.put("input", text);
		payload.put("voice", voice);
		payload.put("response_format", "mp3");
		try {
			byte[] audio = http.post()
					.uri("/v1/audio/speech")
					.header("Authorization", "Bearer " + properties.openaiApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.parseMediaType("audio/mpeg"), MediaType.APPLICATION_OCTET_STREAM)
					.body(payload)
					.retrieve()
					.body(byte[].class);
			if (audio == null || audio.length == 0) {
				throw new SpeechSynthesisException("OpenAI trả audio rỗng");
			}
			return audio;
		} catch (RestClientResponseException e) {
			// Không nối cause: thông báo của nó chứa thân phản hồi.
			log.warn("OpenAI TTS trả HTTP {}", e.getStatusCode().value());
			throw new SpeechSynthesisException("OpenAI TTS trả HTTP " + e.getStatusCode().value());
		} catch (RestClientException e) {
			log.warn("Không gọi được OpenAI TTS: {}", e.getClass().getSimpleName());
			throw new SpeechSynthesisException("Không gọi được OpenAI TTS: " + e.getClass().getSimpleName(), e);
		}
	}

	private static boolean isRateLimited(SpeechSynthesisException e) {
		return e.getMessage() != null && e.getMessage().contains(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new SpeechSynthesisException("Bị ngắt khi chờ retry OpenAI TTS", ie);
		}
	}
}
