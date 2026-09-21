package vn.enlearning.backend.ai;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import vn.enlearning.backend.entity.enums.ChatRole;
import vn.enlearning.backend.entity.enums.StudySkill;

/**
 * Trợ lý chat bằng Gemini. Nhận ngữ cảnh (đã giới hạn ở {@code ChatService}) và chỉ gợi ý NHÓM bài;
 * đường dẫn cụ thể do {@code ChatLinkResolver} chọn từ DB. Lỗi Gemini báo {@link AiUnavailableException}.
 */
@Component
@Conditional(GeminiKeyPresent.class)
class GeminiChatAssistant implements ChatAssistant {

	private static final double TEMPERATURE = 0.5;
	private static final int MAX_SKILLS = 3;
	private static final Set<StudySkill> SUGGESTABLE = Set.of(StudySkill.VOCABULARY, StudySkill.WRITING,
			StudySkill.LISTENING, StudySkill.READING, StudySkill.SPEAKING, StudySkill.EXAM);

	private static final Map<String, Object> SCHEMA = Map.of(
			"type", "OBJECT",
			"properties", Map.of(
					"content", Map.of("type", "STRING"),
					"suggestedSkills", Map.of("type", "ARRAY", "items", Map.of("type", "STRING", "enum",
							List.of("VOCABULARY", "WRITING", "LISTENING", "READING", "SPEAKING", "EXAM"))),
					"refusal", Map.of("type", "BOOLEAN")),
			"required", List.of("content", "suggestedSkills", "refusal"));

	private final GeminiClient client;
	private final String systemPrompt = loadPrompt("ai/chat-assistant.txt");

	GeminiChatAssistant(GeminiClient client) {
		this.client = client;
	}

	@Override
	public Reply reply(List<Turn> history, String message) {
		GeminiClient.Result result;
		try {
			result = client.generateJson(systemPrompt, turns(history, message), SCHEMA, TEMPERATURE);
		} catch (GeminiException e) {
			throw new AiUnavailableException(e.getMessage(), e);
		}
		JsonNode json = result.json();
		String content = json.path("content").asString("").strip();
		if (content.isEmpty()) {
			throw new AiUnavailableException("Gemini trả câu trả lời rỗng");
		}
		boolean refusal = json.path("refusal").asBoolean(false);
		return new Reply(content, refusal ? List.of() : skills(json.path("suggestedSkills")), refusal,
				result.modelName(), result.promptTokens(), result.completionTokens());
	}

	/** Gộp các lượt liền nhau cùng vai (tin gửi lỗi trước đó) và bỏ lượt đầu nếu là của trợ lý: Gemini cần bắt đầu bằng người dùng. */
	private static List<GeminiClient.Turn> turns(List<Turn> history, String message) {
		List<GeminiClient.Turn> turns = new ArrayList<>();
		for (Turn turn : history) {
			String role = turn.role() == ChatRole.ASSISTANT ? "model" : "user";
			if (turns.isEmpty() && "model".equals(role)) {
				continue;
			}
			append(turns, role, turn.content());
		}
		append(turns, "user", message);
		return turns;
	}

	private static void append(List<GeminiClient.Turn> turns, String role, String text) {
		int last = turns.size() - 1;
		if (last >= 0 && turns.get(last).role().equals(role)) {
			turns.set(last, new GeminiClient.Turn(role, turns.get(last).text() + "\n\n" + text));
		} else {
			turns.add(new GeminiClient.Turn(role, text));
		}
	}

	private static List<StudySkill> skills(JsonNode array) {
		Set<StudySkill> skills = new LinkedHashSet<>();
		for (JsonNode item : array) {
			try {
				StudySkill skill = StudySkill.valueOf(item.asString("").strip().toUpperCase(Locale.ROOT));
				if (SUGGESTABLE.contains(skill) && skills.size() < MAX_SKILLS) {
					skills.add(skill);
				}
			} catch (IllegalArgumentException e) {
				// Bỏ qua giá trị lạ.
			}
		}
		return List.copyOf(skills);
	}

	private static String loadPrompt(String path) {
		try {
			return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("Không đọc được prompt " + path, e);
		}
	}
}
