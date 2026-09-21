package vn.enlearning.backend.flashcard.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import vn.enlearning.backend.entity.enums.RecallLevel;
import vn.enlearning.backend.flashcard.service.ReviewScheduler.Schedule;

class ReviewSchedulerTest {

	private static final Instant NOW = Instant.parse("2026-09-21T00:00:00Z");

	@Test
	void notRememberedResetsAndRetriesSoon() {
		Schedule s = ReviewScheduler.next(RecallLevel.NOT_REMEMBERED, 30, NOW);
		assertThat(s.intervalDays()).isZero();
		assertThat(s.nextReviewAt()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));
	}

	@Test
	void almostRememberedStartsAtOneDayThenKeepsInterval() {
		assertThat(ReviewScheduler.next(RecallLevel.ALMOST_REMEMBERED, 0, NOW).intervalDays()).isEqualTo(1);
		assertThat(ReviewScheduler.next(RecallLevel.ALMOST_REMEMBERED, 14, NOW).intervalDays()).isEqualTo(14);
	}

	@Test
	void rememberedClimbsTheLadderAndStopsAtTheTop() {
		int interval = 0;
		int[] seen = new int[8];
		for (int i = 0; i < seen.length; i++) {
			interval = ReviewScheduler.next(RecallLevel.REMEMBERED, interval, NOW).intervalDays();
			seen[i] = interval;
		}
		assertThat(seen).containsExactly(3, 7, 14, 30, 60, 120, 180, 180);
		assertThat(ReviewScheduler.next(RecallLevel.REMEMBERED, 1, NOW).nextReviewAt())
				.isEqualTo(NOW.plus(Duration.ofDays(3)));
	}
}
