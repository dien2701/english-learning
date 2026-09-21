package vn.enlearning.backend.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import vn.enlearning.backend.chat.repository.ChatMessageRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.ChatMessage;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.WritingPrompt;
import vn.enlearning.backend.entity.enums.ChatRole;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.security.JwtService;

/**
 * Security → Controller → Service → JPA trên MySQL thật, với bản giả của trợ lý AI. Dữ liệu tự rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatApiTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private JwtService jwt;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private WritingPromptRepository prompts;
	@Autowired
	private ChatMessageRepository messages;
	@Autowired
	private EntityManager em;

	private String tokenA;
	private String tokenB;

	@BeforeEach
	void setUp() {
		tokenA = "Bearer " + jwt.issueAccessToken(newUser());
		tokenB = "Bearer " + jwt.issueAccessToken(newUser());

		// Bảo đảm có ít nhất một đề viết ACTIVE để trợ lý có thứ gợi ý, dù DB có seed hay không.
		Topic topic = new Topic();
		topic.setSlug("C" + UUID.randomUUID().toString().replace("-", ""));
		topic.setNameVi("Chủ đề chat");
		topics.save(topic);
		WritingPrompt prompt = new WritingPrompt();
		prompt.setTopic(topic);
		prompt.setTitleVi("Đề chat");
		prompt.setLevel(Level.BEGINNER);
		prompt.setStatus(ContentStatus.ACTIVE);
		prompt.setInstructions("Write.");
		prompts.saveAndFlush(prompt);
	}

	@Test
	@DisplayName("Câu hỏi gợi ý; hội thoại mới rỗng, tiêu đề mặc định")
	void startersAndCreate() throws Exception {
		call(get("/chat/starters"), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", hasSize(4)));
		call(post("/chat/conversations"), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("Hội thoại mới"))
				.andExpect(jsonPath("$.data.messageCount").value(0))
				.andExpect(jsonPath("$.data.messages", hasSize(0)));
	}

	@Test
	@DisplayName("Gửi câu hỏi tiếng Anh: có trả lời, gợi ý bài học thật, tiêu đề từ tin đầu, lưu model và token")
	void sendInScope() throws Exception {
		String id = create(tokenA);
		call(send(id, "How do I use the present perfect?"), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.userMessage.role").value("USER"))
				.andExpect(jsonPath("$.data.userMessage.links").doesNotExist())
				.andExpect(jsonPath("$.data.userMessage.isRefusal").doesNotExist())
				.andExpect(jsonPath("$.data.reply.role").value("ASSISTANT"))
				.andExpect(jsonPath("$.data.reply.isRefusal").value(false))
				.andExpect(jsonPath("$.data.reply.links[0].path", startsWith("/writing/")))
				.andExpect(jsonPath("$.data.reply.links[0].skill").value("WRITING"));
		em.flush();
		em.clear();

		call(get("/chat/conversations/" + id), tokenA)
				.andExpect(jsonPath("$.data.title").value("How do I use the present perfect?"))
				.andExpect(jsonPath("$.data.messageCount").value(2))
				.andExpect(jsonPath("$.data.messages[0].role").value("USER"))
				.andExpect(jsonPath("$.data.messages[1].role").value("ASSISTANT"));

		List<ChatMessage> saved = messages.findByConversationIdOrderByCreatedAtAscIdAsc(UUID.fromString(id));
		assertThat(saved).hasSize(2);
		assertThat(saved.get(0).getModelName()).isNull();
		assertThat(saved.get(0).getPromptTokens()).isNull();
		assertThat(saved.get(1).getModelName()).isEqualTo("fake-chat-assistant");
		assertThat(saved.get(1).getPromptTokens()).isPositive();
		assertThat(saved.get(1).getCompletionTokens()).isPositive();
		assertThat(saved.get(1).getLinks()).isNotEmpty();
	}

	@Test
	@DisplayName("Câu ngoài phạm vi: từ chối lịch sự (isRefusal), không có gợi ý; danh sách có xem trước và số tin")
	void refusalAndList() throws Exception {
		String id = create(tokenA);
		call(send(id, "Who won the last World Cup?"), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.reply.isRefusal").value(true))
				.andExpect(jsonPath("$.data.reply.links").doesNotExist());

		call(get("/chat/conversations"), tokenA)
				.andExpect(jsonPath("$.data", hasSize(1)))
				.andExpect(jsonPath("$.data[0].id").value(id))
				.andExpect(jsonPath("$.data[0].messageCount").value(2))
				.andExpect(jsonPath("$.data[0].preview").isNotEmpty());
	}

	@Test
	@DisplayName("Tin rỗng, quá dài: 400 kèm khoá lỗi; không có token: 401")
	void validation() throws Exception {
		String id = create(tokenA);
		call(send(id, "   "), tokenA)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.content").value("errors.field.messageRequired"));
		call(send(id, "a".repeat(2001)), tokenA)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.content").value("errors.field.messageTooLong"));
		call(get("/chat/conversations"), null).andExpect(status().isUnauthorized());
		call(post("/chat/conversations"), null).andExpect(status().isUnauthorized());
		call(send(id, "hello english"), null).andExpect(status().isUnauthorized());
		call(get("/chat/conversations/" + id), tokenA)
				.andExpect(jsonPath("$.data.messageCount").value(0));
	}

	@Test
	@DisplayName("Hội thoại của người khác: 404 ở xem, gửi, đổi tên, xoá; danh sách của họ rỗng")
	void otherUsersConversation() throws Exception {
		String id = create(tokenA);
		call(get("/chat/conversations/" + id), tokenB).andExpect(status().isNotFound());
		call(send(id, "english grammar"), tokenB).andExpect(status().isNotFound());
		call(rename(id, "x"), tokenB).andExpect(status().isNotFound());
		call(delete("/chat/conversations/" + id), tokenB).andExpect(status().isNotFound());
		call(get("/chat/conversations"), tokenB).andExpect(jsonPath("$.data", hasSize(0)));
		call(get("/chat/conversations/" + id), tokenA).andExpect(status().isOk());
	}

	@Test
	@DisplayName("AI lỗi: 503 AI_UNAVAILABLE nhưng tin của người học vẫn được lưu; gửi lại được")
	void aiFailureKeepsUserMessage() throws Exception {
		String id = create(tokenA);
		call(send(id, "english grammar [fail]"), tokenA)
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
		em.flush();
		em.clear();
		call(get("/chat/conversations/" + id), tokenA)
				.andExpect(jsonPath("$.data.messageCount").value(1))
				.andExpect(jsonPath("$.data.messages[0].role").value("USER"));
		call(send(id, "english grammar"), tokenA).andExpect(status().isOk());
	}

	@Test
	@DisplayName("Đổi tiêu đề và xoá mềm: xoá xong 404 và biến khỏi danh sách nhưng tin nhắn vẫn còn trong DB")
	void renameAndSoftDelete() throws Exception {
		String id = create(tokenA);
		call(send(id, "english vocabulary"), tokenA).andExpect(status().isOk());
		call(rename(id, "  Từ vựng  "), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.title").value("Từ vựng"));
		call(rename(id, " "), tokenA)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrorKeys.title").value("errors.field.titleRequired"));

		call(delete("/chat/conversations/" + id), tokenA)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.deleted").value(true));
		em.flush();
		em.clear();
		call(get("/chat/conversations/" + id), tokenA).andExpect(status().isNotFound());
		call(send(id, "english"), tokenA).andExpect(status().isNotFound());
		call(get("/chat/conversations"), tokenA).andExpect(jsonPath("$.data", hasSize(0)));
		assertThat(messages.findByConversationIdOrderByCreatedAtAscIdAsc(UUID.fromString(id))).hasSize(2);
	}

	@Test
	@DisplayName("Tin thứ hai không đổi tiêu đề; tin nhắn theo đúng thứ tự")
	void titleFromFirstMessageOnly() throws Exception {
		String id = create(tokenA);
		call(send(id, "english first question"), tokenA).andExpect(status().isOk());
		call(send(id, "english second question"), tokenA).andExpect(status().isOk());
		em.flush();
		em.clear();
		call(get("/chat/conversations/" + id), tokenA)
				.andExpect(jsonPath("$.data.title").value("english first question"))
				.andExpect(jsonPath("$.data.messages", hasSize(4)))
				.andExpect(jsonPath("$.data.messages[2].content").value("english second question"))
				.andExpect(jsonPath("$.data.messages[3].role").value(ChatRole.ASSISTANT.name()));
	}

	private String create(String token) throws Exception {
		String body = call(post("/chat/conversations"), token).andReturn().getResponse().getContentAsString();
		return body.replaceAll(".*\"data\":\\{\"id\":\"([^\"]+)\".*", "$1");
	}

	private static MockHttpServletRequestBuilder send(String id, String content) {
		return post("/chat/conversations/" + id + "/messages").contentType(MediaType.APPLICATION_JSON)
				.content("{\"content\":\"" + content + "\"}");
	}

	private static MockHttpServletRequestBuilder rename(String id, String title) {
		return patch("/chat/conversations/" + id).contentType(MediaType.APPLICATION_JSON)
				.content("{\"title\":\"" + title + "\"}");
	}

	private ResultActions call(MockHttpServletRequestBuilder request, String token) throws Exception {
		if (token != null) {
			request.header("Authorization", token);
		}
		return mvc.perform(request);
	}

	private User newUser() {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		return users.saveAndFlush(user);
	}
}
