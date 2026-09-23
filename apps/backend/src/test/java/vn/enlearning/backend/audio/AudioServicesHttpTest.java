package vn.enlearning.backend.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

/** Chạy OpenAI TTS và Cloudinary qua máy chủ HTTP giả (không gọi ra ngoài), cùng bản lưu cục bộ. */
class AudioServicesHttpTest {

	private HttpServer server;
	private volatile int status = 200;
	private volatile byte[] responseBody = new byte[0];
	private volatile String responseType = "application/json";
	private final AtomicReference<String> method = new AtomicReference<>();
	private final AtomicReference<String> path = new AtomicReference<>();
	private final AtomicReference<String> authorization = new AtomicReference<>();
	private final AtomicReference<String> contentType = new AtomicReference<>();
	private final AtomicReference<String> body = new AtomicReference<>();

	@BeforeEach
	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", exchange -> {
			method.set(exchange.getRequestMethod());
			path.set(exchange.getRequestURI().getPath());
			authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
			contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
			body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
			exchange.getResponseHeaders().add("Content-Type", responseType);
			exchange.sendResponseHeaders(status, responseBody.length == 0 ? -1 : responseBody.length);
			if (responseBody.length > 0) {
				exchange.getResponseBody().write(responseBody);
			}
			exchange.close();
		});
		server.start();
	}

	@AfterEach
	void stop() {
		server.stop(0);
	}

	private String baseUrl() {
		return "http://127.0.0.1:" + server.getAddress().getPort();
	}

	private AudioProperties properties(String openaiKey, String cloudinaryUrl, String localDir) {
		return new AudioProperties(openaiKey, "tts-test", baseUrl(), Duration.ofSeconds(5), cloudinaryUrl, baseUrl(),
				Duration.ofSeconds(5), localDir, false);
	}

	// --- OpenAI -----------------------------------------------------------------------------------

	@Test
	@DisplayName("OpenAI TTS: gửi đúng đường dẫn, key, model, giọng, mp3; trả byte audio")
	void openAiSynthesizes() {
		responseType = "audio/mpeg";
		responseBody = new byte[] { 1, 2, 3 };

		byte[] audio = new OpenAiSpeechSynthesizer(properties("sk-test", "", "x")).synthesize("Hello there", "nova");

		assertThat(audio).containsExactly(1, 2, 3);
		assertThat(method.get()).isEqualTo("POST");
		assertThat(path.get()).isEqualTo("/v1/audio/speech");
		assertThat(authorization.get()).isEqualTo("Bearer sk-test");
		assertThat(body.get()).contains("\"model\":\"tts-test\"", "\"input\":\"Hello there\"", "\"voice\":\"nova\"",
				"\"response_format\":\"mp3\"");
	}

	@Test
	@DisplayName("OpenAI TTS: HTTP lỗi hoặc thân rỗng thành SpeechSynthesisException, thông báo không lộ thân phản hồi")
	void openAiFailures() {
		OpenAiSpeechSynthesizer synthesizer = new OpenAiSpeechSynthesizer(properties("sk-test", "", "x"));

		status = 429;
		responseBody = "secret-detail".getBytes(StandardCharsets.UTF_8);
		assertThatThrownBy(() -> synthesizer.synthesize("Hi", "alloy"))
				.isInstanceOf(SpeechSynthesisException.class).hasMessageContaining("429").hasMessageNotContaining("secret-detail");

		status = 200;
		responseBody = new byte[0];
		assertThatThrownBy(() -> synthesizer.synthesize("Hi", "alloy")).isInstanceOf(SpeechSynthesisException.class);
	}

	// --- Cloudinary -------------------------------------------------------------------------------

	private CloudinaryAudioStorage cloudinary() {
		return new CloudinaryAudioStorage(properties("", "cloudinary://key123:abcd@demo_cloud", "x"), JsonMapper.builder().build());
	}

	@Test
	@DisplayName("Cloudinary: chữ ký SHA-1 đúng ví dụ trong tài liệu của Cloudinary")
	void signsLikeCloudinaryDocs() {
		Map<String, String> params = Map.of("eager", "w_400,h_300,c_pad|w_260,h_200,c_crop", "public_id", "sample_image",
				"timestamp", "1315060510");
		assertThat(cloudinary().sign(params)).isEqualTo("bfd09f95f331f558cbd1320e67aa8d488770583e");
	}

	@Test
	@DisplayName("Cloudinary: tải lên gửi multipart tới /v1_1/<cloud>/video/upload với public_id trong thư mục, api_key và chữ ký; trả secure_url")
	void cloudinaryUploads() {
		responseBody = "{\"secure_url\":\"https://res.cloudinary.com/demo_cloud/video/upload/v1/En-Learning/abc.mp3\",\"public_id\":\"En-Learning/abc\"}"
				.getBytes(StandardCharsets.UTF_8);

		StoredAudio stored = cloudinary().store(new byte[] { 9, 8, 7 }, "abc", "mp3");

		assertThat(stored.url()).isEqualTo("https://res.cloudinary.com/demo_cloud/video/upload/v1/En-Learning/abc.mp3");
		assertThat(stored.publicId()).isEqualTo("En-Learning/abc");
		assertThat(path.get()).isEqualTo("/v1_1/demo_cloud/video/upload");
		assertThat(contentType.get()).startsWith("multipart/form-data");
		assertThat(body.get()).contains("name=\"public_id\"", "En-Learning/abc", "name=\"asset_folder\"", "name=\"api_key\"", "key123",
				"name=\"timestamp\"", "name=\"signature\"", "name=\"file\"", "filename=\"abc.mp3\"");
		assertThat(body.get()).doesNotContain("abcd"); // API secret không bao giờ được gửi đi
	}

	@Test
	@DisplayName("Cloudinary: tải lên lỗi thành AudioStorageException; xoá chỉ gọi cho file trong thư mục của ứng dụng và không ném lỗi")
	void cloudinaryErrorsAndDelete() {
		CloudinaryAudioStorage storage = cloudinary();

		status = 401;
		responseBody = "{\"error\":{\"message\":\"Invalid Signature\"}}".getBytes(StandardCharsets.UTF_8);
		assertThatThrownBy(() -> storage.store(new byte[] { 1 }, "abc", "mp3"))
				.isInstanceOf(AudioStorageException.class).hasMessageContaining("401").hasMessageNotContaining("Signature");

		status = 200;
		responseBody = "{\"result\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
		storage.delete("en-learning/listening/abc");
		assertThat(path.get()).isEqualTo("/v1_1/demo_cloud/video/destroy");
		assertThat(contentType.get()).startsWith("application/x-www-form-urlencoded");
		assertThat(body.get()).contains("public_id=en-learning%2Flistening%2Fabc", "api_key=key123", "signature=");

		path.set(null);
		storage.delete("someone-elses/asset");
		storage.delete("abc-123.mp3"); // audio cục bộ cũ
		storage.delete(null);
		assertThat(path.get()).isNull();

		status = 500;
		storage.delete("en-learning/listening/abc"); // lỗi mạng/máy chủ chỉ ghi log
	}

	@Test
	@DisplayName("CLOUDINARY_URL sai dạng làm app không khởi động, thông báo không chứa giá trị")
	void rejectsMalformedCloudinaryUrl() {
		assertThatThrownBy(() -> new CloudinaryAudioStorage(properties("", "not-a-url-supersecret", "x"), JsonMapper.builder().build()))
				.isInstanceOf(IllegalStateException.class).hasMessageNotContaining("supersecret");
	}

	// --- Lưu cục bộ -------------------------------------------------------------------------------

	@Test
	@DisplayName("Lưu cục bộ: ghi file, trả URL /media/audio/ kèm context-path, xoá theo publicId, từ chối tên/đuôi lạ và đường dẫn lên trên")
	void localStorage(@TempDir Path dir) throws IOException {
		LocalAudioStorage storage = new LocalAudioStorage(properties("", "", dir.toString()), "/api");

		StoredAudio stored = storage.store(new byte[] { 1, 2, 3 }, "lesson-1", "mp3");

		assertThat(stored.url()).isEqualTo("/api/media/audio/lesson-1.mp3");
		assertThat(stored.publicId()).isEqualTo("lesson-1.mp3");
		assertThat(Files.readAllBytes(dir.resolve("lesson-1.mp3"))).containsExactly(1, 2, 3);

		assertThatThrownBy(() -> storage.store(new byte[] { 1 }, "../evil", "mp3")).isInstanceOf(AudioStorageException.class);
		assertThatThrownBy(() -> storage.store(new byte[] { 1 }, "ok", "exe")).isInstanceOf(AudioStorageException.class);

		Path outside = Files.writeString(dir.resolveSibling("keep-" + System.nanoTime() + ".mp3"), "x");
		storage.delete("../" + outside.getFileName());
		assertThat(outside).exists();
		Files.delete(outside);

		storage.delete("lesson-1.mp3");
		assertThat(dir.resolve("lesson-1.mp3")).doesNotExist();
		storage.delete("lesson-1.mp3"); // xoá lần nữa không lỗi
	}
}
