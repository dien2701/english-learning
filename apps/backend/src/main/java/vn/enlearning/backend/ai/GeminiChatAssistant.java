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
import java.util.function.Consumer;

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
			"propertyOrdering", List.of("content", "suggestedSkills", "refusal"),
				"required", List.of("content", "suggestedSkills", "refusal"));

	private final GeminiClient client;
	private final String systemPrompt = loadPrompt("ai/chat-assistant.txt");

	GeminiChatAssistant(GeminiClient client) {
		this.client = client;
	}

	@Override
	public Reply reply(List<Turn> history, String message) {
		return reply(history, message, null);
	}

	@Override
	public Reply reply(List<Turn> history, String message, Consumer<String> onContent) {
		GeminiClient.Result result;
		try {
			result = onContent == null
					? client.generateJson(systemPrompt, turns(history, message), SCHEMA, TEMPERATURE)
					: client.streamJson(systemPrompt, turns(history, message), SCHEMA, TEMPERATURE,
							new ContentStream(onContent));
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

	/** Rút giá trị chuỗi "content" từ JSON đang đến dở và chỉ báo phần chữ mới. */
	private static final class ContentStream implements Consumer<String> {

		private final Consumer<String> sink;
		private int sent;

		ContentStream(Consumer<String> sink) {
			this.sink = sink;
		}

		@Override
		public void accept(String json) {
			String content = partialContent(json);
			if (content.length() > sent) {
				sink.accept(content.substring(sent));
				sent = content.length();
			}
		}
	}

	/** Giải mã phần đã có của chuỗi "content"; dừng ở escape chưa đủ ký tự. */
	static String partialContent(String json) {
		int key = json.indexOf("\"content\"");
		int colon = key < 0 ? -1 : json.indexOf(':', key + 9);
		int quote = colon < 0 ? -1 : json.indexOf('"', colon + 1);
		if (quote < 0) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		for (int i = quote + 1; i < json.length(); i++) {
			char c = json.charAt(i);
			if (c == '"') {
				break;
			}
			if (c != '\\') {
				out.append(c);
				continue;
			}
			if (i + 1 >= json.length()) {
				break;
			}
			char e = json.charAt(++i);
			switch (e) {
				case 'n' -> out.append('\n');
				case 't' -> out.append('\t');
				case 'r' -> out.append('\r');
				case 'u' -> {
					if (i + 4 >= json.length()) {
						return out.toString();
					}
					out.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
					i += 4;
				}
				default -> out.append(e);
			}
		}
		return out.toString();
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
