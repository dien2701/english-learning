package vn.enlearning.backend.audio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AudioSource;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.security.JwtService;

/**
 * Sinh, tải lên và xoá audio bài nghe qua Security → Controller → Service → JPA trên MySQL thật, với TTS được thay
 * bằng mock và lưu cục bộ vào {@code target/test-audio}. Dữ liệu tự dựng trong giao dịch test (tự rollback);
 * file audio đã ghi được dọn ở {@link #cleanUp()}. Việc xoá file cũ chạy sau commit nên không thấy trong test này.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ListeningAudioApiTests {

	private static final String TRANSCRIPT = "Agent: Good morning.\\nMinh: Hello there.\\nAgent: Have a nice trip.";

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private ListeningLessonRepository lessons;
	@Autowired
	private JwtService jwt;
	@Autowired
	private AudioProperties audioProperties;
	@MockitoBean
	private SpeechSynthesizer synthesizer;

	private String admin;
	private String learner;
	private String lessonId;
	private byte[] silence;

	@BeforeEach
	void setUp() throws Exception {
		silence = new ClassPathResource("audio/silence.mp3").getContentAsByteArray();
		when(synthesizer.synthesize(anyString(), anyString())).thenReturn(silence);
		admin = "Bearer " + jwt.issueAccessToken(newUser(Role.ADMIN));
		learner = "Bearer " + jwt.issueAccessToken(newUser(Role.USER));
		String topicId = data(json(post("/admin/topics"), admin, "{\"nameVi\":\"Chủ đề âm thanh\",\"nameEn\":\"Audio topic\"}")
				.andExpect(status().isCreated()).andReturn());
		lessonId = data(json(post("/admin/content"), admin, listening(topicId, "")).andExpect(status().isCreated()).andReturn());
	}

	@AfterEach
	void cleanUp() throws IOException {
		Path dir = Path.of(audioProperties.localDir());
		if (lessonId == null || !Files.isDirectory(dir)) {
			return;
		}
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, lessonId + "-*")) {
			for (Path file : files) {
				Files.deleteIfExists(file);
			}
		}
	}

	// --- 401 / 403 / 404 --------------------------------------------------------------------------

	@Test
	@DisplayName("Ba endpoint audio: 401 khi chưa đăng nhập, 403 với USER, 404 với bài không tồn tại")
	void security() throws Exception {
		String base = "/admin/listening/" + lessonId + "/audio";
		MockMultipartFile mp3 = new MockMultipartFile("file", "a.mp3", "audio/mpeg", silence);
		for (String token : new String[] { null, learner }) {
			int expected = token == null ? 401 : 403;
			perform(post(base + "/generate"), token).andExpect(status().is(expected));
			perform(multipart(base).file(mp3), token).andExpect(status().is(expected));
			perform(delete(base), token).andExpect(status().is(expected));
		}
		String missing = "/admin/listening/" + UUID.randomUUID() + "/audio";
		perform(post(missing + "/generate"), admin).andExpect(status().isNotFound());
		perform(multipart(missing).file(mp3), admin).andExpect(status().isNotFound());
		perform(delete(missing), admin).andExpect(status().isNotFound());
	}

	// --- sinh bằng TTS ----------------------------------------------------------------------------

	@Test
	@DisplayName("Sinh audio: mỗi người nói một giọng, ghép thành file, cập nhật audioUrl/audioSource/duration, file có thể tải không cần token")
	void generate() throws Exception {
		MvcResult result = perform(post("/admin/listening/" + lessonId + "/audio/generate"), admin)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(lessonId))
				.andExpect(jsonPath("$.data.audioSource").value("TTS"))
				.andExpect(jsonPath("$.data.durationSeconds").value(3))
				.andReturn();
		String url = JsonPath.read(result.getResponse().getContentAsString(), "$.data.audioUrl");
		assertThat(url).startsWith("/api/media/audio/" + lessonId + "-").endsWith(".mp3");

		ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> voice = ArgumentCaptor.forClass(String.class);
		verify(synthesizer, org.mockito.Mockito.times(3)).synthesize(text.capture(), voice.capture());
		assertThat(text.getAllValues()).containsExactly("Good morning.", "Hello there.", "Have a nice trip.");
		assertThat(voice.getAllValues()).containsExactly("alloy", "echo", "alloy");

		ListeningLesson lesson = lessons.findById(UUID.fromString(lessonId)).orElseThrow();
		assertThat(lesson.getAudioSource()).isEqualTo(AudioSource.TTS);
		assertThat(lesson.getAudioPublicId()).startsWith(lessonId).endsWith(".mp3");
		assertThat(lesson.getDurationSeconds()).isEqualTo(3);

		String file = url.substring("/api".length());
		perform(get(file), null).andExpect(status().isOk()).andExpect(content().contentType("audio/mpeg"))
				.andExpect(r -> assertThat(r.getResponse().getContentAsByteArray()).hasSize(3 * silence.length));
		perform(get(file).header("Range", "bytes=0-9"), null).andExpect(status().isPartialContent());
		perform(get("/media/audio/khong-co.mp3"), null).andExpect(status().isNotFound());

		perform(get("/admin/content/" + lessonId), admin).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.audioSource").value("TTS"))
				.andExpect(jsonPath("$.data.payload.audioUrl").value(url));
	}

	@Test
	@DisplayName("Sinh lại: tên file mới, audioPublicId đổi theo")
	void regenerate() throws Exception {
		perform(post("/admin/listening/" + lessonId + "/audio/generate"), admin).andExpect(status().isOk());
		String first = lessons.findById(UUID.fromString(lessonId)).orElseThrow().getAudioPublicId();
		Thread.sleep(5); // tên file mang mốc thời gian mili giây
		perform(post("/admin/listening/" + lessonId + "/audio/generate"), admin).andExpect(status().isOk());
		assertThat(lessons.findById(UUID.fromString(lessonId)).orElseThrow().getAudioPublicId()).isNotEqualTo(first);
	}

	@Test
	@DisplayName("TTS lỗi thì 503 AI_UNAVAILABLE và không đổi audio hiện có")
	void generateFailsWhenSynthesizerFails() throws Exception {
		when(synthesizer.synthesize(any(), any())).thenThrow(new SpeechSynthesisException("boom"));

		perform(post("/admin/listening/" + lessonId + "/audio/generate"), admin)
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
		assertThat(lessons.findById(UUID.fromString(lessonId)).orElseThrow().getAudioUrl()).isNull();
	}

	@Test
	@DisplayName("Sửa bài (PUT) không đè hay xoá audio do TTS/tải lên quản lý, kể cả khi gửi audioUrl khác")
	void putKeepsManagedAudio() throws Exception {
		perform(post("/admin/listening/" + lessonId + "/audio/generate"), admin).andExpect(status().isOk());
		String url = lessons.findById(UUID.fromString(lessonId)).orElseThrow().getAudioUrl();

		String topicId = JsonPath.read(perform(get("/admin/content/" + lessonId), admin).andReturn().getResponse().getContentAsString(),
				"$.data.payload.topicId");
		json(put("/admin/content/" + lessonId), admin, listening(topicId, "https://example.com/other.mp3"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.payload.audioUrl").value(url));
		json(put("/admin/content/" + lessonId), admin, listening(topicId, ""))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data.audioSource").value("TTS"))
				.andExpect(jsonPath("$.data.payload.audioUrl").value(url));
	}

	// --- tải lên ----------------------------------------------------------------------------------

	@Test
	@DisplayName("Tải lên mp3: audioSource UPLOAD, đo thời lượng từ file, thay được nhiều lần")
	void upload() throws Exception {
		MockMultipartFile mp3 = new MockMultipartFile("file", "lesson.mp3", "audio/mpeg", silence);
		perform(multipart("/admin/listening/" + lessonId + "/audio").file(mp3), admin)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.audioSource").value("UPLOAD"))
				.andExpect(jsonPath("$.data.durationSeconds").value(1))
				.andExpect(jsonPath("$.data.audioUrl").value(org.hamcrest.Matchers.startsWith("/api/media/audio/" + lessonId + "-")));
		String first = lessons.findById(UUID.fromString(lessonId)).orElseThrow().getAudioPublicId();

		Thread.sleep(5);
		perform(multipart("/admin/listening/" + lessonId + "/audio").file(mp3), admin).andExpect(status().isOk());
		assertThat(lessons.findById(UUID.fromString(lessonId)).orElseThrow().getAudioPublicId()).isNotEqualTo(first);
	}

	@Test
	@DisplayName("Tải lên: thiếu file 400, loại lạ hoặc nội dung không khớp 415, quá 20MB 413")
	void uploadValidation() throws Exception {
		String url = "/admin/listening/" + lessonId + "/audio";
		perform(multipart(url), admin).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.file").value("errors.field.fileRequired"));
		perform(multipart(url).file(new MockMultipartFile("file", "a.mp3", "audio/mpeg", new byte[0])), admin)
				.andExpect(status().isBadRequest());
		perform(multipart(url).file(new MockMultipartFile("file", "a.txt", "text/plain", "hello".getBytes())), admin)
				.andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.code").value("AUDIO_UNSUPPORTED"));
		perform(multipart(url).file(new MockMultipartFile("file", "a.mp3", "audio/mpeg", "day khong phai mp3".getBytes())), admin)
				.andExpect(status().isUnsupportedMediaType());
		perform(multipart(url).file(new MockMultipartFile("file", "a.mp3", "audio/mpeg", new byte[20 * 1024 * 1024 + 1])), admin)
				.andExpect(status().isPayloadTooLarge()).andExpect(jsonPath("$.code").value("AUDIO_TOO_LARGE"));
		// Đã bị từ chối hết nên bài vẫn chưa có audio.
		assertThat(lessons.findById(UUID.fromString(lessonId)).orElseThrow().getAudioUrl()).isNull();
	}

	// --- xoá --------------------------------------------------------------------------------------

	@Test
	@DisplayName("Xoá audio: audioUrl/audioSource về null, thời lượng giữ nguyên, xoá lần nữa vẫn 200")
	void remove() throws Exception {
		perform(post("/admin/listening/" + lessonId + "/audio/generate"), admin).andExpect(status().isOk());

		perform(delete("/admin/listening/" + lessonId + "/audio"), admin)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.audioUrl").doesNotExist())
				.andExpect(jsonPath("$.data.audioSource").doesNotExist())
				.andExpect(jsonPath("$.data.durationSeconds").value(3));
		ListeningLesson lesson = lessons.findById(UUID.fromString(lessonId)).orElseThrow();
		assertThat(lesson.getAudioUrl()).isNull();
		assertThat(lesson.getAudioSource()).isNull();
		assertThat(lesson.getAudioPublicId()).isNull();

		perform(delete("/admin/listening/" + lessonId + "/audio"), admin).andExpect(status().isOk());
		perform(get("/admin/content/" + lessonId), admin).andExpect(jsonPath("$.data.audioSource").doesNotExist());
	}

	// --- dữ liệu mẫu ------------------------------------------------------------------------------

	private static String listening(String topicId, String audioUrl) {
		return "{\"skill\":\"LISTENING\",\"titleVi\":\"Bài nghe " + UUID.randomUUID() + "\",\"titleEn\":\"Listening\",\"level\":\"BEGINNER\","
				+ "\"topicId\":\"" + topicId + "\",\"audioUrl\":\"" + audioUrl + "\",\"durationSeconds\":30,\"transcript\":\"" + TRANSCRIPT
				+ "\",\"questions\":[{\"kind\":\"SINGLE_CHOICE\",\"content\":\"Q?\",\"options\":["
				+ "{\"content\":\"Yes\",\"correct\":true},{\"content\":\"No\",\"correct\":false}]},"
				+ "{\"kind\":\"FILL_BLANK\",\"content\":\"Fill ___\",\"acceptedAnswers\":[\"apple\"]}]}";
	}

	private User newUser(Role role) {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		user.setRole(role);
		return users.saveAndFlush(user);
	}

	/** Nhận cả builder thường lẫn builder multipart (hai lớp riêng nhau), gắn Bearer token nếu có. */
	private ResultActions perform(RequestBuilder builder, String token) throws Exception {
		return mvc.perform(context -> {
			MockHttpServletRequest request = builder.buildRequest(context);
			if (token != null) {
				request.addHeader("Authorization", token);
			}
			return request;
		});
	}

	private ResultActions json(MockHttpServletRequestBuilder request, String token, String body) throws Exception {
		return perform(request.contentType(MediaType.APPLICATION_JSON).content(body), token);
	}

	private static String data(MvcResult result) throws Exception {
		return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
	}
}
