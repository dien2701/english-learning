package vn.enlearning.backend.study.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.StudySkill;
import vn.enlearning.backend.study.dto.HeartbeatRequest;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/** Luật cộng giờ: tối đa 60 giây mỗi heartbeat, im lặng quá 2 phút hoặc đổi nội dung thì mở phiên mới. */
class StudySessionServiceTest {

	private final UUID userId = UUID.randomUUID();
	private final UUID deckId = UUID.randomUUID();
	private final List<StudySession> saved = new ArrayList<>();
	private Instant now;
	private StudySessionService service;

	@BeforeEach
	void setUp() {
		now = Instant.parse("2026-09-21T08:00:00Z");
		StudySessionRepository sessions = mock(StudySessionRepository.class);
		when(sessions.findFirstByUserIdOrderByLastHeartbeatAtDesc(eq(userId)))
				.thenAnswer(inv -> saved.isEmpty() ? Optional.empty() : Optional.of(saved.get(saved.size() - 1)));
		when(sessions.save(any(StudySession.class))).thenAnswer(inv -> {
			StudySession s = inv.getArgument(0);
			if (s.getId() == null) {
				s.setId(UUID.randomUUID());
				saved.add(s);
			}
			return s;
		});
		UserRepository users = mock(UserRepository.class);
		when(users.getReferenceById(userId)).thenReturn(new User());
		Clock clock = new Clock() {
			@Override
			public java.time.ZoneId getZone() {
				return ZoneOffset.UTC;
			}

			@Override
			public Clock withZone(java.time.ZoneId zone) {
				return this;
			}

			@Override
			public Instant instant() {
				return now;
			}
		};
		service = new StudySessionService(sessions, users, clock);
	}

	private void beat(long secondsLater, UUID ref) {
		now = now.plus(Duration.ofSeconds(secondsLater));
		service.heartbeat(userId, new HeartbeatRequest(StudySkill.VOCABULARY, ref));
	}

	@Test
	void creditsElapsedTimeCappedAtSixtySeconds() {
		beat(0, deckId);
		beat(30, deckId);
		assertThat(saved).hasSize(1);
		assertThat(saved.get(0).getActiveSeconds()).isEqualTo(30);
		beat(100, deckId);
		assertThat(saved).hasSize(1);
		assertThat(saved.get(0).getActiveSeconds()).isEqualTo(90);
	}

	@Test
	void silenceOverTwoMinutesOpensNewSession() {
		beat(0, deckId);
		beat(121, deckId);
		assertThat(saved).hasSize(2);
		assertThat(saved.get(1).getActiveSeconds()).isZero();
	}

	@Test
	void changingContentOpensNewSession() {
		beat(0, deckId);
		beat(10, UUID.randomUUID());
		assertThat(saved).hasSize(2);
	}
}
