package vn.enlearning.backend.audio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vn.enlearning.backend.audio.TranscriptVoicePlanner.Utterance;

class TranscriptVoicePlannerTest {

	@Test
	@DisplayName("Mỗi người nói một giọng theo thứ tự xuất hiện; giọng tái sử dụng khi người đó nói lại; bỏ tên khỏi lời đọc")
	void assignsVoicePerSpeaker() {
		List<Utterance> plan = TranscriptVoicePlanner.plan("""
				Agent: Good morning.
				Minh: Good morning. I'm flying to Singapore.
				Agent: May I see your passport?
				Minh: Here you are.
				""");

		assertThat(plan).extracting(Utterance::voice).containsExactly("alloy", "echo", "alloy", "echo");
		assertThat(plan).extracting(Utterance::text).containsExactly(
				"Good morning.", "Good morning. I'm flying to Singapore.", "May I see your passport?", "Here you are.");
	}

	@Test
	@DisplayName("Các dòng liền nhau của cùng người nói được gộp; tên không phân biệt hoa thường")
	void mergesConsecutiveLines() {
		List<Utterance> plan = TranscriptVoicePlanner.plan("Tom: Hello.\r\nTom: How are you?\r\n\r\nTOM: Fine.\nAnna: Hi.");

		assertThat(plan).hasSize(2);
		assertThat(plan.get(0).text()).isEqualTo("Hello. How are you? Fine.");
		assertThat(plan.get(1).voice()).isEqualTo("echo");
	}

	@Test
	@DisplayName("Dòng không có tên đọc tiếp lời người nói trước; bài không có tên nào dùng giọng người kể")
	void unnamedLines() {
		List<Utterance> lecture = TranscriptVoicePlanner.plan("Lecturer: Today we talk about cities.\nThey are warm.");
		assertThat(lecture).hasSize(1);
		assertThat(lecture.get(0).text()).isEqualTo("Today we talk about cities. They are warm.");
		assertThat(lecture.get(0).voice()).isEqualTo("alloy");

		List<Utterance> announcement = TranscriptVoicePlanner.plan("May I have your attention, please. The 6:45 express is delayed.");
		assertThat(announcement).hasSize(1);
		assertThat(announcement.get(0).voice()).isEqualTo(TranscriptVoicePlanner.NARRATOR_VOICE);
		assertThat(announcement.get(0).text()).startsWith("May I have your attention");
	}

	@Test
	@DisplayName("Câu có dấu hai chấm nhưng không phải tên người nói (chữ thường, quá dài) không bị tách")
	void colonInsideSentence() {
		List<Utterance> plan = TranscriptVoicePlanner.plan("Remember this rule: always check the time.");
		assertThat(plan).hasSize(1);
		assertThat(plan.get(0).voice()).isEqualTo(TranscriptVoicePlanner.NARRATOR_VOICE);
		assertThat(plan.get(0).text()).isEqualTo("Remember this rule: always check the time.");
	}

	@Test
	@DisplayName("Đoạn dài hơn giới hạn được cắt ở cuối câu, mỗi mảnh không vượt MAX_CHARS")
	void splitsLongText() {
		String sentence = "This is a fairly ordinary sentence about the weather today. ";
		String text = sentence.repeat(80).trim(); // ~4800 ký tự
		List<Utterance> plan = TranscriptVoicePlanner.plan("Lecturer: " + text);

		assertThat(plan.size()).isGreaterThan(2);
		assertThat(plan).allSatisfy(u -> {
			assertThat(u.text().length()).isLessThanOrEqualTo(TranscriptVoicePlanner.MAX_CHARS);
			assertThat(u.text()).endsWith(".");
			assertThat(u.voice()).isEqualTo("alloy");
		});
		assertThat(String.join(" ", plan.stream().map(Utterance::text).toList())).isEqualTo(text);
	}

	@Test
	@DisplayName("Từ dài không có khoảng trắng vẫn bị cắt cứng, không lặp vô hạn")
	void hardCut() {
		List<String> parts = TranscriptVoicePlanner.split("a".repeat(TranscriptVoicePlanner.MAX_CHARS * 2 + 10));
		assertThat(parts).hasSize(3);
		assertThat(parts.get(0)).hasSize(TranscriptVoicePlanner.MAX_CHARS);
	}

	@Test
	@DisplayName("Transcript rỗng hoặc chỉ có tên không có lời thì không có đoạn nào")
	void emptyPlan() {
		assertThat(TranscriptVoicePlanner.plan("   \n\n")).isEmpty();
		assertThat(TranscriptVoicePlanner.plan("Tom:\nAnna:")).isEmpty();
	}
}
