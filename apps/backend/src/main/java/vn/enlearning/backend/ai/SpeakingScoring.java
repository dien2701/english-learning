package vn.enlearning.backend.ai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Tổng hợp điểm cả lượt từ kết quả từng câu. Câu đọc theo mẫu nên: điểm tổng và phát âm là trung bình điểm các
 * câu; từ vựng, ngữ pháp, bám sát đề là mức khớp giữa transcript và câu mẫu (thang 10); trôi chảy lấy theo điểm
 * trung bình các câu. Không gọi AI.
 */
public final class SpeakingScoring {

	/** Điểm thang 10, một chữ số thập phân. */
	public record Scores(BigDecimal overall, BigDecimal pronunciation, BigDecimal vocabulary, BigDecimal grammar,
			BigDecimal fluency, BigDecimal relevance) {
	}

	private SpeakingScoring() {
	}

	/** @param prompts không rỗng */
	public static Scores aggregate(List<SpeakingGrader.PromptSummary> prompts) {
		double scoreSum = 0;
		double matchSum = 0;
		for (SpeakingGrader.PromptSummary p : prompts) {
			scoreSum += p.assessment().score().doubleValue();
			matchSum += matchRatio(p.promptText(), p.assessment().transcript());
		}
		BigDecimal average = scale(scoreSum / prompts.size());
		BigDecimal match = scale(10.0 * matchSum / prompts.size());
		return new Scores(average, average, match, match, average, match);
	}

	/** Tỉ lệ từ của câu mẫu xuất hiện đúng thứ tự trong transcript (dãy con chung dài nhất), 0-1. */
	static double matchRatio(String expected, String transcript) {
		List<String> a = words(expected);
		List<String> b = words(transcript);
		if (a.isEmpty()) {
			return 1.0;
		}
		int[][] lcs = new int[a.size() + 1][b.size() + 1];
		for (int i = 1; i <= a.size(); i++) {
			for (int j = 1; j <= b.size(); j++) {
				lcs[i][j] = a.get(i - 1).equals(b.get(j - 1)) ? lcs[i - 1][j - 1] + 1
						: Math.max(lcs[i - 1][j], lcs[i][j - 1]);
			}
		}
		return lcs[a.size()][b.size()] / (double) a.size();
	}

	private static List<String> words(String text) {
		if (text == null) {
			return List.of();
		}
		return java.util.Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}']+"))
				.filter(w -> !w.isEmpty()).toList();
	}

	private static BigDecimal scale(double value) {
		return BigDecimal.valueOf(Math.max(0.0, Math.min(10.0, value))).setScale(1, RoundingMode.HALF_UP);
	}
}
