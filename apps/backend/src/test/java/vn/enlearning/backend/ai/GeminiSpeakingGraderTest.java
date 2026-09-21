package vn.enlearning.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

/** Chạy {@code GeminiSpeakingGrader} qua {@code GeminiClient} thật vào một máy chủ HTTP giả, không gọi ra ngoài. */
class GeminiSpeakingGraderTest {

	private static final byte[] AUDIO = { 1, 2, 3, 4 };

	private HttpServer server;
	private volatile int status = 200;
	private volatile String responseBody = "";
	private final AtomicReference<String> requestBody = new AtomicReference<>();
	private GeminiSpeakingGrader grader;

	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", exchange -> {
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
		AiProperties properties = new AiProperties("test-key", "gemini-test",
				"http://127.0.0.1:" + server.getAddress().getPort(), Duration.ofSeconds(5), Duration.ofSeconds(60),
				Duration.ZERO);
		grader = new GeminiSpeakingGrader(new GeminiClient(properties, JsonMapper.builder().build()));
	}

	@AfterEach
	void stop() {
		server.stop(0);
	}

	/** Bọc JSON kết quả vào khuôn phản hồi {@code generateContent}. */
	private static String envelope(String json) {
		return "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":"
				+ JsonMapper.builder().build().writeValueAsString(json)
				+ "}]}}],\"usageMetadata\":{\"promptTokenCount\":10,\"candidatesTokenCount\":5}}";
	}

	private SpeakingGrader.PromptAssessment assess() {
		return grader.assessPrompt("I think three is enough.", "a.webm", "audio/webm", AUDIO);
	}

	@Test
	@DisplayName("Đọc transcript, điểm (kẹp 0-10, làm tròn 0.5), từ lỗi; gửi kèm audio base64 và câu mẫu")
	void parsesAssessment() {
		responseBody = envelope("""
				{"transcript":"I think tree is enough.","score":6.74,
				 "wordIssues":[
				  {"word":"three","heardAs":"tree","issue":"Âm /θ/ đọc thành /t/","tip":"Đặt lưỡi giữa hai hàm răng."},
				  {"word":"","heardAs":"x","issue":"y","tip":"z"},
				  {"word":"enough","heardAs":"","issue":"","tip":"z"}],
				 "tips":["Giữ nhịp đều.", "  "]}""");

		SpeakingGrader.PromptAssessment result = assess();

		assertThat(result.transcript()).isEqualTo("I think tree is enough.");
		assertThat(result.score()).isEqualByComparingTo("6.5");
		assertThat(result.wordIssues()).hasSize(1);
		assertThat(result.wordIssues().get(0).word()).isEqualTo("three");
		assertThat(result.wordIssues().get(0).heardAs()).isEqualTo("tree");
		assertThat(result.tips()).containsExactly("Giữ nhịp đều.");
		assertThat(result.modelName()).isEqualTo("gemini-test");
		assertThat(requestBody.get())
				.contains("\"inlineData\"")
				.contains("\"mimeType\":\"audio/webm\"")
				.contains(Base64.getEncoder().encodeToString(AUDIO))
				.contains("I think three is enough.");

		responseBody = envelope("{\"transcript\":\"\",\"score\":-3,\"wordIssues\":[],\"tips\":[]}");
		assertThat(assess().score()).isEqualByComparingTo("0.0");
	}

	@Test
	@DisplayName("Thiếu điểm hoặc thiếu transcript, HTTP lỗi: AiGradingException (không lộ key)")
	void assessmentFailures() {
		responseBody = envelope("{\"transcript\":\"hi\",\"wordIssues\":[],\"tips\":[]}");
		assertThatThrownBy(this::assess).isInstanceOf(AiGradingException.class);

		responseBody = envelope("{\"score\":7,\"wordIssues\":[],\"tips\":[]}");
		assertThatThrownBy(this::assess).isInstanceOf(AiGradingException.class);

		status = 503;
		responseBody = "{\"error\":{\"message\":\"overloaded\"}}";
		assertThatThrownBy(this::assess)
				.isInstanceOf(AiGradingException.class)
				.hasMessageContaining("503")
				.hasMessageNotContaining("test-key");
	}

	@Test
	@DisplayName("writeImprovements: đọc danh sách (tối đa 4), rỗng thì lỗi; nội dung gửi chứa kết quả từng câu")
	void writesImprovements() {
		SpeakingGrader.PromptSummary summary = new SpeakingGrader.PromptSummary("I think three is enough.",
				new SpeakingGrader.PromptAssessment("I think tree is enough.", java.math.BigDecimal.valueOf(6.5),
						List.of(new vn.enlearning.backend.entity.SpeakingWordIssue("three", "tree", "Âm /θ/", "Lưỡi")),
						List.of(), "gemini-test"));
		responseBody = envelope("{\"improvements\":[\"a\",\"b\",\"c\",\"d\",\"e\"]}");

		List<String> improvements = grader.writeImprovements("Bài 1", List.of(summary));

		assertThat(improvements).containsExactly("a", "b", "c", "d");
		assertThat(requestBody.get()).contains("I think tree is enough.").contains("three").doesNotContain("inlineData");

		responseBody = envelope("{\"improvements\":[]}");
		assertThatThrownBy(() -> grader.writeImprovements("Bài 1", List.of(summary)))
				.isInstanceOf(AiGradingException.class);
	}
}
