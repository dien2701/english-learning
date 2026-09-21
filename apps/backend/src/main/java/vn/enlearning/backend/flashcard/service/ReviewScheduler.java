package vn.enlearning.backend.flashcard.service;

import java.time.Duration;
import java.time.Instant;

import vn.enlearning.backend.entity.enums.RecallLevel;

/**
 * Lịch ôn tập dạng SM-2 đơn giản theo 3 mức tự đánh giá.
 * <ul>
 * <li>NOT_REMEMBERED: về 0 ngày, ôn lại sau 10 phút.</li>
 * <li>ALMOST_REMEMBERED: lần đầu 1 ngày, các lần sau giữ nguyên khoảng cũ.</li>
 * <li>REMEMBERED: lên bậc kế tiếp của {@link #LADDER} (3, 7, 14, 30, 60, 120, 180 ngày).</li>
 * </ul>
 */
final class ReviewScheduler {

	static final int[] LADDER = { 3, 7, 14, 30, 60, 120, 180 };
	static final Duration RETRY_DELAY = Duration.ofMinutes(10);

	record Schedule(int intervalDays, Instant nextReviewAt) {
	}

	private ReviewScheduler() {
	}

	static Schedule next(RecallLevel level, int previousIntervalDays, Instant now) {
		return switch (level) {
			case NOT_REMEMBERED -> new Schedule(0, now.plus(RETRY_DELAY));
			case ALMOST_REMEMBERED -> inDays(previousIntervalDays <= 0 ? 1 : previousIntervalDays, now);
			case REMEMBERED -> inDays(stepUp(previousIntervalDays), now);
		};
	}

	private static int stepUp(int previous) {
		for (int rung : LADDER) {
			if (rung > previous) {
				return rung;
			}
		}
		return LADDER[LADDER.length - 1];
	}

	private static Schedule inDays(int days, Instant now) {
		return new Schedule(days, now.plus(Duration.ofDays(days)));
	}
}
