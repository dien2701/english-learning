package vn.enlearning.backend.ai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Gọi REST {@code generateContent} của Gemini, ép trả JSON đúng khuôn bằng {@code responseSchema}.
 * Chỉ nạp khi có {@code app.ai.gemini-api-key}. Log không in key hay nội dung gửi/nhận.
 */
@Slf4j
@Component
@Conditional(GeminiKeyPresent.class)
public class GeminiClient {

	private static final String API_KEY_HEADER = "x-goog-api-key";

	private final AiProperties properties;
	private final JsonMapper jsonMapper;
	private final RestClient http;

	public GeminiClient(AiProperties properties, JsonMapper jsonMapper) {
		this.properties = properties;
		this.jsonMapper = jsonMapper;
		HttpClient httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(properties.geminiTimeout())
				.build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(properties.geminiTimeout());
		this.http = RestClient.builder().baseUrl(properties.geminiBaseUrl()).requestFactory(factory).build();
	}

	/**
	 * Một lượt hội thoại gửi lên; {@code role} là {@code user} hoặc {@code model}. {@code audio} (có thể null) là
	 * âm thanh gửi kèm dạng inline.
	 */
	public record Turn(String role, String text, Audio audio) {

		public Turn(String role, String text) {
			this(role, text, null);
		}

		public static Turn user(String text) {
			return new Turn("user", text);
		}

		public static Turn model(String text) {
			return new Turn("model", text);
		}

		public static Turn userWithAudio(String text, String mimeType, byte[] data) {
			return new Turn("user", text, new Audio(mimeType, data));
		}
	}

	/** Âm thanh gửi inline (mã hoá base64 khi gửi); chỉ nằm trong bộ nhớ suốt lần gọi. */
	public record Audio(String mimeType, byte[] data) {
	}

	/** JSON model trả về (đã đúng khuôn schema) cùng số token và tên model đã dùng. */
	public record Result(JsonNode json, int promptTokens, int completionTokens, String modelName) {
	}

	/**
	 * @param systemPrompt   chỉ dẫn hệ thống
	 * @param turns          nội dung hội thoại, lượt cuối là của người dùng
	 * @param responseSchema schema kiểu OpenAPI (kiểu viết HOA: OBJECT, STRING, NUMBER, ARRAY)
	 * @throws GeminiException khi không nhận được JSON hợp lệ
	 */
	public Result generateJson(String systemPrompt, List<Turn> turns, Map<String, Object> responseSchema,
			double temperature) {
		Map<String, Object> payload = payload(systemPrompt, turns, responseSchema, temperature);
		return withFallback(model -> parse(retry(() -> postOnce(payload, model)), model));
	}

	/**
	 * Như {@link #generateJson} nhưng nhận theo luồng SSE; {@code onText} nhận TOÀN BỘ văn bản JSON thô tích luỹ
	 * mỗi khi có thêm đoạn mới. Thử lại/đổi model chỉ xảy ra trước khi có đoạn nào (503/429 trả ngay ở HTTP).
	 */
	public Result streamJson(String systemPrompt, List<Turn> turns, Map<String, Object> responseSchema,
			double temperature, Consumer<String> onText) {
		Map<String, Object> payload = payload(systemPrompt, turns, responseSchema, temperature);
		return withFallback(model -> retry(() -> streamOnce(payload, model, onText)));
	}

	private Map<String, Object> payload(String systemPrompt, List<Turn> turns, Map<String, Object> responseSchema,
			double temperature) {
		List<Object> contents = new ArrayList<>();
		for (Turn turn : turns) {
			List<Object> parts = new ArrayList<>();
			parts.add(Map.of("text", turn.text()));
			if (turn.audio() != null) {
				parts.add(Map.of("inlineData", Map.of("mimeType", turn.audio().mimeType(), "data",
						Base64.getEncoder().encodeToString(turn.audio().data()))));
			}
			contents.add(Map.of("role", turn.role(), "parts", parts));
		}
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
		payload.put("contents", contents);
		payload.put("generationConfig", Map.of(
				"responseMimeType", MediaType.APPLICATION_JSON_VALUE,
				"responseSchema", responseSchema,
				"temperature", temperature));
		return payload;
	}

	private Result withFallback(Function<String, Result> call) {
		String model = properties.geminiModel();
		try {
			return call.apply(model);
		} catch (TransientGeminiException e) {
			String fallback = properties.geminiFallbackModel();
			if (fallback.isBlank()) {
				throw new GeminiException(e.getMessage());
			}
			log.warn("Gemini {} quá tải, chuyển sang {}", model, fallback);
			try {
				return call.apply(fallback);
			} catch (TransientGeminiException e2) {
				throw new GeminiException(e2.getMessage());
			}
		}
	}

	/** 503/429 là quá tải tạm thời phía Gemini: thử lại tối đa 3 lần, chờ 1s rồi 2s. */
	private <T> T retry(Supplier<T> call) {
		for (int attempt = 1;; attempt++) {
			try {
				return call.get();
			} catch (TransientGeminiException e) {
				if (attempt == 3) {
					throw e;
				}
				try {
					Thread.sleep(1000L * attempt);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new GeminiException(e.getMessage());
				}
			}
		}
	}

	private static class TransientGeminiException extends RuntimeException {
		TransientGeminiException(String message) {
			super(message);
		}
	}

	private String postOnce(Map<String, Object> payload, String model) {
		try {
			return http.post()
					.uri("/v1beta/models/{model}:generateContent", model)
					.header(API_KEY_HEADER, properties.geminiApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(jsonMapper.writeValueAsString(payload))
					.retrieve()
					.body(String.class);
		} catch (RestClientResponseException e) {
			// Không nối cause: thông báo của nó chứa thân phản hồi.
			int status = e.getStatusCode().value();
			log.warn("Gemini trả HTTP {}", status);
			if (status == 503 || status == 429) {
				throw new TransientGeminiException("Gemini trả HTTP " + status);
			}
			throw new GeminiException("Gemini trả HTTP " + e.getStatusCode().value());
		} catch (RestClientException | JacksonException e) {
			log.warn("Không gọi được Gemini: {}", e.getClass().getSimpleName());
			throw new GeminiException("Không gọi được Gemini: " + e.getClass().getSimpleName(), e);
		}
	}

	private Result streamOnce(Map<String, Object> payload, String model, Consumer<String> onText) {
		try {
			return http.post()
					.uri("/v1beta/models/{model}:streamGenerateContent?alt=sse", model)
					.header(API_KEY_HEADER, properties.geminiApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(jsonMapper.writeValueAsString(payload))
					.exchange((request, response) -> {
						int status = response.getStatusCode().value();
						if (status == 503 || status == 429) {
							log.warn("Gemini trả HTTP {}", status);
							throw new TransientGeminiException("Gemini trả HTTP " + status);
						}
						if (status >= 400) {
							log.warn("Gemini trả HTTP {}", status);
							throw new GeminiException("Gemini trả HTTP " + status);
						}
						StringBuilder text = new StringBuilder();
						String finishReason = "STOP";
						JsonNode usage = null;
						try (BufferedReader reader = new BufferedReader(
								new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
							for (String line = reader.readLine(); line != null; line = reader.readLine()) {
								if (!line.startsWith("data:")) {
									continue;
								}
								JsonNode chunk = jsonMapper.readTree(line.substring(5).strip());
								JsonNode candidate = chunk.path("candidates").path(0);
								if (candidate.isMissingNode()) {
									throw new GeminiException("Gemini không trả ứng viên nào (có thể bị chặn nội dung)");
								}
								if (candidate.hasNonNull("finishReason")) {
									finishReason = candidate.path("finishReason").asString("STOP");
								}
								if (chunk.has("usageMetadata")) {
									usage = chunk.path("usageMetadata");
								}
								int before = text.length();
								for (JsonNode part : candidate.path("content").path("parts")) {
									text.append(part.path("text").asString(""));
								}
								if (text.length() > before) {
									onText.accept(text.toString());
								}
							}
						}
						return result(text.toString(), finishReason, usage, model);
					}, true);
		} catch (RestClientException | JacksonException e) {
			log.warn("Không gọi được Gemini: {}", e.getClass().getSimpleName());
			throw new GeminiException("Không gọi được Gemini: " + e.getClass().getSimpleName(), e);
		}
	}

	private Result parse(String response, String model) {
		try {
			JsonNode root = jsonMapper.readTree(response);
			JsonNode candidate = root.path("candidates").path(0);
			if (candidate.isMissingNode()) {
				throw new GeminiException("Gemini không trả ứng viên nào (có thể bị chặn nội dung)");
			}
			StringBuilder text = new StringBuilder();
			for (JsonNode part : candidate.path("content").path("parts")) {
				text.append(part.path("text").asString(""));
			}
			return result(text.toString(), candidate.path("finishReason").asString("STOP"), root.path("usageMetadata"),
					model);
		} catch (JacksonException e) {
			throw new GeminiException("Gemini trả JSON sai khuôn", e);
		}
	}

	private Result result(String text, String finishReason, JsonNode usage, String model) {
		if (!"STOP".equals(finishReason)) {
			throw new GeminiException("Gemini dừng bất thường: " + finishReason);
		}
		if (text.isEmpty()) {
			throw new GeminiException("Gemini trả nội dung rỗng");
		}
		int promptTokens = usage == null ? 0 : usage.path("promptTokenCount").asInt(0);
		int completionTokens = usage == null ? 0
				: usage.path("candidatesTokenCount").asInt(0) + usage.path("thoughtsTokenCount").asInt(0);
		try {
			return new Result(jsonMapper.readTree(text), promptTokens, completionTokens, model);
		} catch (JacksonException e) {
			throw new GeminiException("Gemini trả JSON sai khuôn", e);
		}
	}
}
