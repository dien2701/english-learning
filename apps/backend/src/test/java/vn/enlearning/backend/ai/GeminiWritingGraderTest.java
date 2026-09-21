package vn.enlearning.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

/** Chạy {@code GeminiWritingGrader} qua {@code GeminiClient} thật vào một máy chủ HTTP giả, không gọi ra ngoài. */
class GeminiWritingGraderTest {

	private HttpServer server;
	private volatile int status = 200;
	private volatile String responseBody = "";
	private final AtomicReference<String> requestPath = new AtomicReference<>();
	private final AtomicReference<String> requestKey = new AtomicReference<>();
	private final AtomicReference<String> requestBody = new AtomicReference<>();
	private GeminiWritingGrader grader;

	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", exchange -> {
			requestPath.set(exchange.getRequestURI().getPath());
			requestKey.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
			requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
			if (bytes.length > 0) {
				exchange.getResponseBody().write(bytes);
			}
			exchange.close();
		});
		server.start();
		AiProperties properties = new AiProperties("test-key", "gemini-test", "http://127.0.0.1:" + server.getAddress().getPort(),
				Duration.ofSeconds(5), Duration.ofSeconds(60), Duration.ZERO);
		grader = new GeminiWritingGrader(new GeminiClient(properties, JsonMapper.builder().build()));
	}

	@AfterEach
	void stop() {
		server.stop(0);
	}

	private static WritingGrader.Request request() {
		return new WritingGrader.Request("Write about your day.", 50, "I goes to school. It is fun.", 6);
	}

	/** Bọc JSON kết quả vào khuôn phản hồi {@code generateContent}. */
	private static String envelope(String json) {
		return "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":"
				+ JsonMapper.builder().build().writeValueAsString(json) + "}]}}],\"usageMetadata\":{\"promptTokenCount\":10,\"candidatesTokenCount\":5}}";
	}

	@Test
	@DisplayName("Đọc điểm, nhận xét, lỗi; kẹp về 0-10 và làm tròn 0.5; gửi đúng đường dẫn và key")
	void parsesGrade() {
		responseBody = envelope("""
				{"overallScore":11.3,"grammarScore":4.74,"vocabularyScore":6,"expressionScore":-1,
				 "summary":"Bài ngắn nhưng rõ ý.",
				 "issues":[
				  {"category":"GRAMMAR","excerpt":"I goes","problem":"Sai chia động từ.","suggestion":"I go"},
				  {"category":"UNKNOWN","excerpt":"x","problem":"y","suggestion":"z"},
				  {"category":"VOCABULARY","excerpt":"","problem":"y","suggestion":"z"}]}""");

		WritingGrader.Grade grade = grader.grade(request());

		assertThat(grade.overallScore()).isEqualByComparingTo("10.0");
		assertThat(grade.grammarScore()).isEqualByComparingTo("4.5");
		assertThat(grade.vocabularyScore()).isEqualByComparingTo("6.0");
		assertThat(grade.expressionScore()).isEqualByComparingTo("0.0");
		assertThat(grade.summary()).isEqualTo("Bài ngắn nhưng rõ ý.");
		assertThat(grade.issues()).hasSize(1);
		assertThat(grade.issues().get(0).excerpt()).isEqualTo("I goes");
		assertThat(grade.modelName()).isEqualTo("gemini-test");
		assertThat(requestPath.get()).isEqualTo("/v1beta/models/gemini-test:generateContent");
		assertThat(requestKey.get()).isEqualTo("test-key");
		assertThat(requestBody.get()).contains("\"responseMimeType\":\"application/json\"").contains("I goes to school");
	}

	@Test
	@DisplayName("HTTP 429 hoặc 5xx báo AiGradingException, thông báo không chứa key")
	void httpErrorFails() {
		status = 429;
		responseBody = "{\"error\":{\"message\":\"quota\"}}";

		assertThatThrownBy(() -> grader.grade(request()))
				.isInstanceOf(AiGradingException.class)
				.hasMessageContaining("429")
				.hasMessageNotContaining("test-key");
	}

	@Test
	@DisplayName("JSON thiếu điểm hoặc thiếu nhận xét báo AiGradingException")
	void malformedResultFails() {
		responseBody = envelope("{\"overallScore\":7,\"grammarScore\":7,\"vocabularyScore\":7,\"summary\":\"ok\",\"issues\":[]}");
		assertThatThrownBy(() -> grader.grade(request())).isInstanceOf(AiGradingException.class);

		responseBody = envelope("{\"overallScore\":7,\"grammarScore\":7,\"vocabularyScore\":7,\"expressionScore\":7,\"summary\":\" \",\"issues\":[]}");
		assertThatThrownBy(() -> grader.grade(request())).isInstanceOf(AiGradingException.class);
	}

	@Test
	@DisplayName("Không có ứng viên (bị chặn) hoặc text không phải JSON báo AiGradingException")
	void blockedOrGarbageFails() {
		responseBody = "{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}";
		assertThatThrownBy(() -> grader.grade(request())).isInstanceOf(AiGradingException.class);

		responseBody = envelope("không phải json");
		assertThatThrownBy(() -> grader.grade(request())).isInstanceOf(AiGradingException.class);
	}
}
