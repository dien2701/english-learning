package vn.enlearning.backend.flashcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.entity.Flashcard;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;

/** Topic + Flashcard qua toàn bộ chồng Security → Controller → Service → JPA; mỗi test tự rollback. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FlashcardApiTests {

	private static final String PASSWORD = "matkhau-dung-1";
	private static final Pattern TOKEN = Pattern.compile("\"token\":\"([^\"]+)\"");

	@Autowired
	private MockMvc mvc;
	@Autowired
	private UserRepository users;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private FlashcardDeckRepository decks;
	@Autowired
	private FlashcardRepository cards;

	private Topic topic;
	private FlashcardDeck deck;
	private List<Flashcard> deckCards;
	private String token;

	@BeforeEach
	void setUp() throws Exception {
		topic = new Topic();
		topic.setSlug("it-" + UUID.randomUUID());
		topic.setNameVi("Chủ đề thử");
		topic.setNameEn("Test topic");
		topics.saveAndFlush(topic);

		deck = newDeck("Bộ thử", "Test deck", Level.BEGINNER, ContentStatus.ACTIVE);
		deckCards = List.of(newCard(deck, "apple", 1), newCard(deck, "banana", 2), newCard(deck, "cherry", 3));
		token = loginAsNewUser();
	}

	private FlashcardDeck newDeck(String vi, String en, Level level, ContentStatus status) {
		FlashcardDeck d = new FlashcardDeck();
		d.setTopic(topic);
		d.setTitleVi(vi);
		d.setTitleEn(en);
		d.setLevel(level);
		d.setStatus(status);
		return decks.saveAndFlush(d);
	}

	private Flashcard newCard(FlashcardDeck d, String word, int order) {
		Flashcard c = new Flashcard();
		c.setDeck(d);
		c.setWord(word);
		c.setMeaningVi("nghĩa " + word);
		c.setSortOrder(order);
		return cards.saveAndFlush(c);
	}

	private String loginAsNewUser() throws Exception {
		User user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		user.setFullName("Người học thử");
		users.saveAndFlush(user);
		String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(user.getEmail(), PASSWORD)))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		Matcher m = TOKEN.matcher(body);
		assertThat(m.find()).isTrue();
		return "Bearer " + m.group(1);
	}

	private ResultActions getAs(String bearer, String url) throws Exception {
		return mvc.perform(get(url).header("Authorization", bearer));
	}

	private ResultActions postJson(String bearer, String url, String json) throws Exception {
		return mvc.perform(post(url).header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
				.content(json));
	}

	private ResultActions rate(String bearer, UUID deckId, Object cardId, String level) throws Exception {
		return postJson(bearer, "/flashcard/decks/" + deckId + "/progress",
				"{\"flashcardId\":\"%s\",\"recallLevel\":\"%s\"}".formatted(cardId, level));
	}

	private ResultActions finish(UUID deckId, String json) throws Exception {
		return postJson(token, "/flashcard/decks/" + deckId + "/finish", json);
	}

	// --- Topic -----------------------------------------------------------------------------------

	@Test
	@DisplayName("GET /topics trả chủ đề song ngữ kèm số bộ thẻ đang hoạt động")
	void listsTopics() throws Exception {
		newDeck("Bộ ẩn", "Hidden", Level.BEGINNER, ContentStatus.INACTIVE);
		String mine = "$.data[?(@.id=='%s')]".formatted(topic.getId());
		getAs(token, "/topics").andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath(mine + ".name.vi").value("Chủ đề thử"))
				.andExpect(jsonPath(mine + ".name.en").value("Test topic"))
				.andExpect(jsonPath(mine + ".itemCount").value(1));
	}

	// --- Danh sách và chi tiết ------------------------------------------------------------------

	@Test
	@DisplayName("Danh sách lọc theo topic/level/search/status, chỉ thấy bộ ACTIVE, có phân trang")
	void filtersDecks() throws Exception {
		newDeck("Bộ nâng cao", "Advanced deck", Level.ADVANCED, ContentStatus.ACTIVE);
		newDeck("Bộ ẩn", "Hidden", Level.BEGINNER, ContentStatus.INACTIVE);
		String base = "/flashcard/decks?topicId=" + topic.getId();

		getAs(token, base).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.items", hasSize(2)))
				.andExpect(jsonPath("$.data.total").value(2))
				.andExpect(jsonPath("$.data.totalPages").value(1))
				.andExpect(jsonPath("$.data.items[0].status").value("NOT_STARTED"))
				.andExpect(jsonPath("$.data.items[0].totalCards").value(3));
		getAs(token, base + "&level=ADVANCED").andExpect(jsonPath("$.data.items", hasSize(1)));
		getAs(token, base + "&search=advanced").andExpect(jsonPath("$.data.items", hasSize(1)));
		getAs(token, base + "&search=100%25").andExpect(jsonPath("$.data.items", hasSize(0)));
		getAs(token, base + "&status=COMPLETED").andExpect(jsonPath("$.data.items", hasSize(0)));
		getAs(token, base + "&pageSize=1&page=2").andExpect(jsonPath("$.data.items", hasSize(1)))
				.andExpect(jsonPath("$.data.totalPages").value(2));
		getAs(token, base + "&level=NOPE").andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION"));
	}

	@Test
	@DisplayName("Chi tiết bộ thẻ có thẻ theo thứ tự; bộ INACTIVE hoặc không có thì 404")
	void deckDetail() throws Exception {
		getAs(token, "/flashcard/decks/" + deck.getId()).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.cards", hasSize(3)))
				.andExpect(jsonPath("$.data.cards[0].word").value("apple"))
				.andExpect(jsonPath("$.data.cards[0].recallLevel").value(nullValue()))
				.andExpect(jsonPath("$.data.cards[0].meaning.vi").value("nghĩa apple"));

		FlashcardDeck hidden = newDeck("Bộ ẩn", "Hidden", Level.BEGINNER, ContentStatus.INACTIVE);
		getAs(token, "/flashcard/decks/" + hidden.getId()).andExpect(status().isNotFound());
		getAs(token, "/flashcard/decks/" + UUID.randomUUID()).andExpect(status().isNotFound());
	}

	// --- Học ------------------------------------------------------------------------------------

	@Test
	@DisplayName("Đánh giá thẻ cập nhật tiến độ và trạng thái bộ thẻ")
	void rateAndProgress() throws Exception {
		String base = "/flashcard/decks?topicId=" + topic.getId();
		UUID a = deckCards.get(0).getId();

		rate(token, deck.getId(), a, "REMEMBERED").andExpect(status().isOk())
				.andExpect(jsonPath("$.data.learnedCards").value(1))
				.andExpect(jsonPath("$.data.progressPercent").value(33));
		getAs(token, base).andExpect(jsonPath("$.data.items[0].status").value("IN_PROGRESS"));

		// Đánh giá lại cùng thẻ: đổi mức, không sinh dòng mới (unique user+thẻ).
		rate(token, deck.getId(), a, "NOT_REMEMBERED").andExpect(jsonPath("$.data.learnedCards").value(0));
		rate(token, deck.getId(), a, "REMEMBERED");
		rate(token, deck.getId(), deckCards.get(1).getId(), "REMEMBERED");
		rate(token, deck.getId(), deckCards.get(2).getId(), "REMEMBERED")
				.andExpect(jsonPath("$.data.progressPercent").value(100));
		getAs(token, base).andExpect(jsonPath("$.data.items[0].status").value("COMPLETED"));
		getAs(token, "/flashcard/decks/" + deck.getId())
				.andExpect(jsonPath("$.data.cards[0].recallLevel").value("REMEMBERED"));
	}

	@Test
	@DisplayName("Thẻ của bộ khác hoặc không tồn tại: 404; recallLevel sai hoặc thiếu: 400")
	void rejectsBadRating() throws Exception {
		FlashcardDeck other = newDeck("Bộ khác", "Other", Level.BEGINNER, ContentStatus.ACTIVE);
		Flashcard foreign = newCard(other, "zebra", 1);
		rate(token, deck.getId(), foreign.getId(), "REMEMBERED").andExpect(status().isNotFound());
		rate(token, deck.getId(), UUID.randomUUID(), "REMEMBERED").andExpect(status().isNotFound());
		rate(token, deck.getId(), deckCards.get(0).getId(), "MAYBE").andExpect(status().isBadRequest());
		postJson(token, "/flashcard/decks/" + deck.getId() + "/progress", "{}")
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION"));
	}

	@Test
	@DisplayName("Finish liệt kê từ cần ôn (chưa nhớ + gần nhớ); id lạ thì 400")
	void finishListsWordsToReview() throws Exception {
		rate(token, deck.getId(), deckCards.get(0).getId(), "REMEMBERED");
		rate(token, deck.getId(), deckCards.get(1).getId(), "ALMOST_REMEMBERED");
		rate(token, deck.getId(), deckCards.get(2).getId(), "NOT_REMEMBERED");

		finish(deck.getId(), "{}")
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.studiedCards").value(3))
				.andExpect(jsonPath("$.data.totalCards").value(3))
				.andExpect(jsonPath("$.data.completionPercent").value(100))
				.andExpect(jsonPath("$.data.remembered").value(1))
				.andExpect(jsonPath("$.data.almostRemembered").value(1))
				.andExpect(jsonPath("$.data.notRemembered").value(1))
				.andExpect(jsonPath("$.data.wordsToReview", hasSize(2)))
				.andExpect(jsonPath("$.data.wordsToReview[0].word").value("banana"));

		finish(deck.getId(), "{\"studiedIds\":[\"%s\"]}".formatted(deckCards.get(0).getId()))
				.andExpect(jsonPath("$.data.studiedCards").value(1))
				.andExpect(jsonPath("$.data.wordsToReview", hasSize(0)));

		finish(deck.getId(), "{\"studiedIds\":[\"%s\"]}".formatted(UUID.randomUUID()))
				.andExpect(status().isBadRequest());
	}

	// --- Bảo mật --------------------------------------------------------------------------------

	@Test
	@DisplayName("Không có token: 401; tiến độ không lẫn giữa người dùng")
	void securityAndIsolation() throws Exception {
		mvc.perform(get("/topics")).andExpect(status().isUnauthorized());
		mvc.perform(get("/flashcard/decks")).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
		mvc.perform(get("/flashcard/decks/" + deck.getId())).andExpect(status().isUnauthorized());
		mvc.perform(post("/flashcard/decks/" + deck.getId() + "/progress").contentType(MediaType.APPLICATION_JSON)
				.content("{}")).andExpect(status().isUnauthorized());

		rate(token, deck.getId(), deckCards.get(0).getId(), "REMEMBERED").andExpect(status().isOk());
		String other = loginAsNewUser();
		getAs(other, "/flashcard/decks?topicId=" + topic.getId())
				.andExpect(jsonPath("$.data.items[0].status").value("NOT_STARTED"))
				.andExpect(jsonPath("$.data.items[0].learnedCards").value(0));
		getAs(other, "/flashcard/decks/" + deck.getId())
				.andExpect(jsonPath("$.data.cards[0].recallLevel").value(nullValue()));
	}
}
