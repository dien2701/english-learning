package vn.enlearning.backend.ai;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		String response = post(payload);
		return parse(response);
	}

	private String post(Map<String, Object> payload) {
		try {
			return http.post()
					.uri("/v1beta/models/{model}:generateContent", properties.geminiModel())
					.header(API_KEY_HEADER, properties.geminiApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(jsonMapper.writeValueAsString(payload))
					.retrieve()
					.body(String.class);
		} catch (RestClientResponseException e) {
			// Không nối cause: thông báo của nó chứa thân phản hồi.
			log.warn("Gemini trả HTTP {}", e.getStatusCode().value());
			throw new GeminiException("Gemini trả HTTP " + e.getStatusCode().value());
		} catch (RestClientException | JacksonException e) {
			log.warn("Không gọi được Gemini: {}", e.getClass().getSimpleName());
			throw new GeminiException("Không gọi được Gemini: " + e.getClass().getSimpleName(), e);
		}
	}

	private Result parse(String response) {
		try {
			JsonNode root = jsonMapper.readTree(response);
			JsonNode candidate = root.path("candidates").path(0);
			if (candidate.isMissingNode()) {
				throw new GeminiException("Gemini không trả ứng viên nào (có thể bị chặn nội dung)");
			}
			String finishReason = candidate.path("finishReason").asString("STOP");
			if (!"STOP".equals(finishReason)) {
				throw new GeminiException("Gemini dừng bất thường: " + finishReason);
			}
			StringBuilder text = new StringBuilder();
			for (JsonNode part : candidate.path("content").path("parts")) {
				text.append(part.path("text").asString(""));
			}
			if (text.isEmpty()) {
				throw new GeminiException("Gemini trả nội dung rỗng");
			}
			JsonNode usage = root.path("usageMetadata");
			int promptTokens = usage.path("promptTokenCount").asInt(0);
			int completionTokens = usage.path("candidatesTokenCount").asInt(0) + usage.path("thoughtsTokenCount").asInt(0);
			return new Result(jsonMapper.readTree(text.toString()), promptTokens, completionTokens,
					properties.geminiModel());
		} catch (JacksonException e) {
			throw new GeminiException("Gemini trả JSON sai khuôn", e);
		}
	}
}
