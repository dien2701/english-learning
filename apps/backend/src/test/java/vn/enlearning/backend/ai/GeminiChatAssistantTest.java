package vn.enlearning.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import vn.enlearning.backend.entity.enums.ChatRole;
import vn.enlearning.backend.entity.enums.StudySkill;

/** Chạy {@code GeminiChatAssistant} qua {@code GeminiClient} thật vào máy chủ HTTP giả. */
class GeminiChatAssistantTest {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();

	private HttpServer server;
	private volatile int status = 200;
	private volatile String responseBody = "";
	private final AtomicReference<String> requestBody = new AtomicReference<>();
	private GeminiChatAssistant assistant;

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
		AiProperties properties = new AiProperties("test-key", "gemini-test", "http://127.0.0.1:" + server.getAddress().getPort(),
				Duration.ofSeconds(5), Duration.ofSeconds(60), Duration.ZERO);
		assistant = new GeminiChatAssistant(new GeminiClient(properties, MAPPER));
	}

	@AfterEach
	void stop() {
		server.stop(0);
	}

	private static String envelope(String json) {
		return "{\"candidates\":[{\"finishReason\":\"STOP\",\"content\":{\"parts\":[{\"text\":"
				+ MAPPER.writeValueAsString(json)
				+ "}]}}],\"usageMetadata\":{\"promptTokenCount\":42,\"candidatesTokenCount\":20,\"thoughtsTokenCount\":8}}";
	}

	@Test
	@DisplayName("Đọc câu trả lời, gợi ý kỹ năng hợp lệ (bỏ giá trị lạ, CHAT, trùng, quá 3), số token và tên model")
	void parsesReply() {
		responseBody = envelope("""
				{"content":"Dùng present perfect khi...","refusal":false,
				 "suggestedSkills":["WRITING","chat","BOGUS","READING","WRITING","SPEAKING","EXAM"]}""");

		ChatAssistant.Reply reply = assistant.reply(List.of(), "Khi nào dùng present perfect?");

		assertThat(reply.content()).isEqualTo("Dùng present perfect khi...");
		assertThat(reply.refusal()).isFalse();
		assertThat(reply.suggestedSkills()).containsExactly(StudySkill.WRITING, StudySkill.READING, StudySkill.SPEAKING);
		assertThat(reply.modelName()).isEqualTo("gemini-test");
		assertThat(reply.promptTokens()).isEqualTo(42);
		assertThat(reply.completionTokens()).isEqualTo(28);
	}

	@Test
	@DisplayName("Từ chối: refusal true thì bỏ gợi ý kỹ năng")
	void refusalDropsSkills() {
		responseBody = envelope(
				"{\"content\":\"Mình chỉ hỗ trợ học tiếng Anh.\",\"refusal\":true,\"suggestedSkills\":[\"WRITING\"]}");

		ChatAssistant.Reply reply = assistant.reply(List.of(), "Giá vàng hôm nay?");

		assertThat(reply.refusal()).isTrue();
		assertThat(reply.suggestedSkills()).isEmpty();
	}

	@Test
	@DisplayName("Ngữ cảnh: bỏ lượt đầu của trợ lý, gộp lượt liền nhau cùng vai, tin mới nhất ở cuối")
	void buildsContents() {
		responseBody = envelope("{\"content\":\"ok\",\"refusal\":false,\"suggestedSkills\":[]}");

		assistant.reply(List.of(
				new ChatAssistant.Turn(ChatRole.ASSISTANT, "cũ"),
				new ChatAssistant.Turn(ChatRole.USER, "một"),
				new ChatAssistant.Turn(ChatRole.USER, "hai"),
				new ChatAssistant.Turn(ChatRole.ASSISTANT, "đáp")), "ba");

		JsonNode contents = MAPPER.readTree(requestBody.get()).path("contents");
		assertThat(contents).hasSize(3);
		assertThat(contents.path(0).path("role").asString()).isEqualTo("user");
		assertThat(contents.path(0).path("parts").path(0).path("text").asString()).isEqualTo("một\n\nhai");
		assertThat(contents.path(1).path("role").asString()).isEqualTo("model");
		assertThat(contents.path(2).path("parts").path(0).path("text").asString()).isEqualTo("ba");
	}

	@Test
	@DisplayName("HTTP lỗi, nội dung rỗng hoặc JSON sai khuôn báo AiUnavailableException")
	void failures() {
		status = 503;
		responseBody = "{\"error\":{}}";
		assertThatThrownBy(() -> assistant.reply(List.of(), "hi")).isInstanceOf(AiUnavailableException.class);

		status = 200;
		responseBody = envelope("{\"content\":\"  \",\"refusal\":false,\"suggestedSkills\":[]}");
		assertThatThrownBy(() -> assistant.reply(List.of(), "hi")).isInstanceOf(AiUnavailableException.class);

		responseBody = envelope("không phải json");
		assertThatThrownBy(() -> assistant.reply(List.of(), "hi")).isInstanceOf(AiUnavailableException.class);
	}
}
