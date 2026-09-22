package vn.enlearning.backend.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tách transcript {@code Tên: câu nói} thành các đoạn để đọc, mỗi người nói một giọng cố định theo thứ tự xuất hiện.
 * Dòng không có "Tên:" đọc tiếp lời người nói trước đó; nếu là dòng đầu (bài thông báo, bài giảng không tên)
 * thì dùng giọng người kể. Các dòng liền nhau của cùng người nói được gộp, đoạn dài hơn {@link #MAX_CHARS} bị cắt.
 */
public final class TranscriptVoicePlanner {

	/** Giới hạn ký tự một lần gọi TTS (API cho tối đa 4096); chừa dư để cắt ở cuối câu. */
	public static final int MAX_CHARS = 1500;
	public static final String NARRATOR_VOICE = "sage";
	static final List<String> VOICES = List.of("alloy", "echo", "nova", "onyx", "shimmer", "fable", "coral", "ash");

	/** Tên người nói: một hoặc hai từ, mỗi từ viết hoa chữ đầu ("Agent", "Mr. Smith"), để câu như "Note: ..." dài hơn không bị nhận nhầm. */
	private static final Pattern LINE = Pattern.compile(
			"^\\s*(\\p{Lu}[\\p{L}\\p{N}.'-]*(?: \\p{Lu}[\\p{L}\\p{N}.'-]*)?):\\s*(.*)$");

	private TranscriptVoicePlanner() {
	}

	/** Một lần gọi TTS: đọc {@code text} bằng giọng {@code voice}. */
	public record Utterance(String voice, String text) {
	}

	private static final class Turn {
		final String speaker; // null = người kể
		final StringBuilder text = new StringBuilder();

		Turn(String speaker) {
			this.speaker = speaker;
		}
	}

	public static List<Utterance> plan(String transcript) {
		List<Turn> turns = new ArrayList<>();
		String current = null;
		for (String rawLine : transcript.split("\\R")) {
			String line = rawLine.trim();
			if (line.isEmpty()) {
				continue;
			}
			String speaker = current;
			String text = line;
			Matcher m = LINE.matcher(line);
			if (m.matches()) {
				speaker = m.group(1);
				text = m.group(2).trim();
			}
			current = speaker;
			if (text.isEmpty()) {
				continue;
			}
			Turn last = turns.isEmpty() ? null : turns.get(turns.size() - 1);
			if (last == null || !sameSpeaker(last.speaker, speaker)) {
				last = new Turn(speaker);
				turns.add(last);
			}
			if (!last.text.isEmpty()) {
				last.text.append(' ');
			}
			last.text.append(text);
		}

		Map<String, String> voices = new HashMap<>();
		List<Utterance> result = new ArrayList<>();
		for (Turn turn : turns) {
			String voice = turn.speaker == null ? NARRATOR_VOICE
					: voices.computeIfAbsent(turn.speaker.toLowerCase(Locale.ROOT), k -> VOICES.get(voices.size() % VOICES.size()));
			for (String chunk : split(turn.text.toString())) {
				result.add(new Utterance(voice, chunk));
			}
		}
		return result;
	}

	private static boolean sameSpeaker(String a, String b) {
		return a == null ? b == null : a.equalsIgnoreCase(b);
	}

	/** Cắt ở cuối câu gần giới hạn nhất; không có thì ở khoảng trắng; không có nữa thì cắt cứng. */
	static List<String> split(String text) {
		List<String> parts = new ArrayList<>();
		String rest = text;
		while (rest.length() > MAX_CHARS) {
			int cut = lastSentenceEnd(rest, MAX_CHARS);
			if (cut <= 0) {
				cut = rest.lastIndexOf(' ', MAX_CHARS);
			}
			if (cut <= 0) {
				cut = MAX_CHARS;
			}
			parts.add(rest.substring(0, cut).trim());
			rest = rest.substring(cut).trim();
		}
		if (!rest.isEmpty()) {
			parts.add(rest);
		}
		return parts;
	}

	/** Vị trí ngay sau dấu kết câu (. ! ?) cuối cùng trong {@code limit} ký tự đầu, chỉ tính khi nằm quá nửa giới hạn. */
	private static int lastSentenceEnd(String text, int limit) {
		for (int i = Math.min(limit, text.length() - 1); i > limit / 2; i--) {
			char c = text.charAt(i - 1);
			if ((c == '.' || c == '!' || c == '?') && Character.isWhitespace(text.charAt(i))) {
				return i;
			}
		}
		return -1;
	}
}
