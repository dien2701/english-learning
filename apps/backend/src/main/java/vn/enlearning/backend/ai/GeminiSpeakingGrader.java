package vn.enlearning.backend.ai;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import vn.enlearning.backend.entity.SpeakingWordIssue;

/**
 * Chấm bài nói bằng Gemini nhận âm thanh inline. Điểm kẹp về 0-10, làm tròn 0.5; phản hồi thiếu trường thì báo
 * {@link AiGradingException}. Âm thanh chỉ nằm trong bộ nhớ suốt lần gọi.
 */
@Component
@Conditional(GeminiKeyPresent.class)
class GeminiSpeakingGrader implements SpeakingGrader {

	private static final double TEMPERATURE = 0.2;
	private static final int MAX_WORD_ISSUES = 8;
	private static final int MAX_TIPS = 3;
	private static final int MAX_IMPROVEMENTS = 4;

	private static final Map<String, Object> ASSESS_SCHEMA = Map.of(
			"type", "OBJECT",
			"properties", Map.of(
					"transcript", Map.of("type", "STRING"),
					"score", Map.of("type", "NUMBER"),
					"wordIssues", Map.of("type", "ARRAY", "items", Map.of(
							"type", "OBJECT",
							"properties", Map.of(
									"word", Map.of("type", "STRING"),
									"heardAs", Map.of("type", "STRING"),
									"issue", Map.of("type", "STRING"),
									"tip", Map.of("type", "STRING")),
							"required", List.of("word", "heardAs", "issue", "tip"))),
					"tips", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))),
			"required", List.of("transcript", "score", "wordIssues", "tips"));

	private static final Map<String, Object> SUMMARY_SCHEMA = Map.of(
			"type", "OBJECT",
			"properties", Map.of("improvements", Map.of("type", "ARRAY", "items", Map.of("type", "STRING"))),
			"required", List.of("improvements"));

	private final GeminiClient client;
	private final String assessPrompt = loadPrompt("ai/speaking-assess.txt");
	private final String summaryPrompt = loadPrompt("ai/speaking-summary.txt");

	GeminiSpeakingGrader(GeminiClient client) {
		this.client = client;
	}

	@Override
	public PromptAssessment assessPrompt(String promptText, String filename, String contentType, byte[] audio) {
		GeminiClient.Result result = call(assessPrompt, List.of(GeminiClient.Turn.userWithAudio(
				"Reference sentence:\n\"\"\"\n" + promptText + "\n\"\"\"\nThe learner's recording is attached as audio.",
				contentType, audio)), ASSESS_SCHEMA);
		JsonNode json = result.json();
		if (!json.path("transcript").isString()) {
			throw new AiGradingException("Gemini thiếu transcript");
		}
		return new PromptAssessment(json.path("transcript").asString("").strip(), score(json.path("score")),
				wordIssues(json.path("wordIssues")), strings(json.path("tips"), MAX_TIPS), result.modelName());
	}

	@Override
	public List<String> writeImprovements(String lessonTitle, List<PromptSummary> prompts) {
		GeminiClient.Result result = call(summaryPrompt,
				List.of(GeminiClient.Turn.user(summaryMessage(lessonTitle, prompts))), SUMMARY_SCHEMA);
		List<String> improvements = strings(result.json().path("improvements"), MAX_IMPROVEMENTS);
		if (improvements.isEmpty()) {
			throw new AiGradingException("Gemini không trả điểm cần cải thiện");
		}
		return improvements;
	}

	private GeminiClient.Result call(String system, List<GeminiClient.Turn> turns, Map<String, Object> schema) {
		try {
			return client.generateJson(system, turns, schema, TEMPERATURE);
		} catch (GeminiException e) {
			throw new AiGradingException(e.getMessage(), e);
		}
	}

	private static String summaryMessage(String lessonTitle, List<PromptSummary> prompts) {
		StringBuilder text = new StringBuilder("Lesson: ").append(lessonTitle).append("\n");
		int i = 1;
		for (PromptSummary p : prompts) {
			PromptAssessment a = p.assessment();
			text.append("\nSentence ").append(i++).append(" (score ").append(a.score()).append("/10)\nReference: ")
					.append(p.promptText()).append("\nHeard: ").append(a.transcript()).append("\nMispronounced: ");
			if (a.wordIssues().isEmpty()) {
				text.append("none");
			}
			for (SpeakingWordIssue w : a.wordIssues()) {
				text.append("\n- ").append(w.word()).append(" (heard as \"").append(w.heardAs()).append("\"): ")
						.append(w.issue());
			}
			text.append("\n");
		}
		return text.toString();
	}

	private static BigDecimal score(JsonNode node) {
		if (!node.isNumber()) {
			throw new AiGradingException("Gemini thiếu điểm câu");
		}
		double clamped = Math.max(0.0, Math.min(10.0, node.asDouble()));
		return BigDecimal.valueOf(Math.round(clamped * 2) / 2.0).setScale(1, RoundingMode.HALF_UP);
	}

	private static List<SpeakingWordIssue> wordIssues(JsonNode array) {
		List<SpeakingWordIssue> issues = new ArrayList<>();
		for (JsonNode item : array) {
			if (issues.size() >= MAX_WORD_ISSUES) {
				break;
			}
			String word = item.path("word").asString("").strip();
			String issue = item.path("issue").asString("").strip();
			if (word.isEmpty() || issue.isEmpty()) {
				continue;
			}
			issues.add(new SpeakingWordIssue(word, item.path("heardAs").asString("").strip(), issue,
					item.path("tip").asString("").strip()));
		}
		return issues;
	}

	private static List<String> strings(JsonNode array, int max) {
		List<String> values = new ArrayList<>();
		for (JsonNode item : array) {
			String value = item.asString("").strip();
			if (!value.isEmpty() && values.size() < max) {
				values.add(value);
			}
		}
		return values;
	}

	private static String loadPrompt(String path) {
		try {
			return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("Không đọc được prompt " + path, e);
		}
	}
}
