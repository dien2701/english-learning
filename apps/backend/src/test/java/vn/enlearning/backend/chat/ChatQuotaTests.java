package vn.enlearning.backend.chat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.security.JwtService;

/** Hạn mức tin nhắn theo ngày, với {@code app.chat.daily-limit=2} và bản giả của trợ lý. Dữ liệu tự rollback. */
@SpringBootTest(properties = "app.chat.daily-limit=2")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatQuotaTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;
	@Autowired
	private EntityManager em;

	private String tokenA;
	private String tokenB;

	@BeforeEach
	void setUp() {
		tokenA = "Bearer " + jwt.issueAccessToken(newUser());
		tokenB = "Bearer " + jwt.issueAccessToken(newUser());
	}

	@Test
	@DisplayName("GET /chat/quota: ban đầu chưa dùng lượt nào; mỗi câu trả lời thành công (kể cả từ chối) trừ một lượt")
	void quotaCountsSuccessfulReplies() throws Exception {
		call(get("/chat/quota"), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.limit").value(2))
				.andExpect(jsonPath("$.data.used").value(0))
				.andExpect(jsonPath("$.data.remaining").value(2))
				.andExpect(jsonPath("$.data.resetAt").exists());
		String id = create(tokenA);

		call(send(id, "english grammar"), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.remaining").value(1));
		call(send(id, "Gia vang hom nay?"), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.reply.isRefusal").value(true))
				.andExpect(jsonPath("$.data.remaining").value(0));

		call(get("/chat/quota"), tokenA)
				.andExpect(jsonPath("$.data.used").value(2))
				.andExpect(jsonPath("$.data.remaining").value(0));
	}

	@Test
	@DisplayName("Hết lượt: 429 CHAT_DAILY_LIMIT, không lưu tin của người học; xoá hội thoại không lấy lại lượt; người khác không ảnh hưởng")
	void limitReached() throws Exception {
		String id = create(tokenA);
		call(send(id, "english grammar"), tokenA).andExpect(status().isOk());
		call(send(id, "english words"), tokenA).andExpect(status().isOk());

		call(send(id, "english again"), tokenA)
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.code").value("CHAT_DAILY_LIMIT"))
				.andExpect(jsonPath("$.messageKey").value("errors.chatDailyLimit"));
		em.flush();
		em.clear();
		call(get("/chat/conversations/" + id), tokenA).andExpect(jsonPath("$.data.messageCount").value(4));

		call(delete("/chat/conversations/" + id), tokenA).andExpect(status().isOk());
		em.flush();
		em.clear();
		call(get("/chat/quota"), tokenA).andExpect(jsonPath("$.data.remaining").value(0));

		call(send(create(tokenB), "english grammar"), tokenB).andExpect(status().isOk());
	}

	@Test
	@DisplayName("AI lỗi: 503, không trừ lượt")
	void aiFailureDoesNotCount() throws Exception {
		String id = create(tokenA);
		call(send(id, "english grammar [fail]"), tokenA).andExpect(status().isServiceUnavailable());

		call(get("/chat/quota"), tokenA)
				.andExpect(jsonPath("$.data.used").value(0))
				.andExpect(jsonPath("$.data.remaining").value(2));
	}

	private String create(String token) throws Exception {
		String body = call(post("/chat/conversations"), token).andReturn().getResponse().getContentAsString();
		return body.replaceAll(".*\"data\":\\{\"id\":\"([^\"]+)\".*", "$1");
	}

	private static MockHttpServletRequestBuilder send(String id, String content) {
		return post("/chat/conversations/" + id + "/messages").contentType(MediaType.APPLICATION_JSON)
				.content("{\"content\":\"" + content + "\"}");
	}

	private ResultActions call(MockHttpServletRequestBuilder request, String token) throws Exception {
		return mvc.perform(request.header("Authorization", token));
	}

	private User newUser() {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		return users.saveAndFlush(user);
	}
}
