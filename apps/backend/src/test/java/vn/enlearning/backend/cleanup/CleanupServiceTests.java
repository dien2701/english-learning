package vn.enlearning.backend.cleanup;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import vn.enlearning.backend.auth.repository.PasswordResetTokenRepository;
import vn.enlearning.backend.auth.repository.RefreshTokenRepository;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.entity.PasswordResetToken;
import vn.enlearning.backend.entity.RefreshToken;
import vn.enlearning.backend.entity.SpeakingAttempt;
import vn.enlearning.backend.entity.SpeakingLesson;
import vn.enlearning.backend.entity.Topic;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.Level;
import vn.enlearning.backend.entity.enums.SpeakingAttemptStatus;
import vn.enlearning.backend.entity.enums.StudySkill;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/**
 * Gọi thẳng {@link CleanupService#cleanUp()} (job định kỳ không chạy trong test) trên MySQL thật, trong giao
 * dịch tự rollback. Ngưỡng mặc định: token giữ 1 ngày sau khi hết hạn, phiên rỗng giữ 30 ngày.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CleanupServiceTests {

	@Autowired
	private CleanupService service;
	@Autowired
	private UserRepository users;
	@Autowired
	private RefreshTokenRepository refreshTokens;
	@Autowired
	private PasswordResetTokenRepository resetTokens;
	@Autowired
	private StudySessionRepository sessions;
	@Autowired
	private SpeakingAttemptRepository speakingAttempts;
	@Autowired
	private SpeakingLessonRepository speakingLessons;
	@Autowired
	private TopicRepository topics;
	@Autowired
	private JdbcTemplate jdbc;
	@Autowired
	private EntityManager entityManager;

	private User user;
	private final Instant now = Instant.now();

	@BeforeEach
	void setUp() {
		user = new User();
		user.setEmail("cleanup-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người Thử");
		user = users.saveAndFlush(user);
	}

	private static String hash() {
		return (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "").substring(0, 64);
	}

	private RefreshToken refreshToken(Duration expiresIn) {
		RefreshToken token = new RefreshToken();
		token.setUser(user);
		token.setTokenHash(hash());
		token.setExpiresAt(now.plus(expiresIn));
		return refreshTokens.saveAndFlush(token);
	}

	private PasswordResetToken resetToken(Duration expiresIn) {
		PasswordResetToken token = new PasswordResetToken();
		token.setUser(user);
		token.setTokenHash(hash());
		token.setExpiresAt(now.plus(expiresIn));
		return resetTokens.saveAndFlush(token);
	}

	private StudySession session(Duration heartbeatAgo, int activeSeconds) {
		StudySession session = new StudySession();
		session.setUser(user);
		session.setSkill(StudySkill.VOCABULARY);
		session.setStartedAt(now.minus(heartbeatAgo));
		session.setLastHeartbeatAt(now.minus(heartbeatAgo));
		session.setActiveSeconds(activeSeconds);
		return sessions.saveAndFlush(session);
	}

	@Test
	@DisplayName("Xoá token hết hạn quá ngưỡng, giữ token còn hạn hoặc mới hết hạn")
	void removesOnlyLongExpiredTokens() {
		RefreshToken longExpired = refreshToken(Duration.ofDays(-2));
		RefreshToken justExpired = refreshToken(Duration.ofHours(-1));
		RefreshToken active = refreshToken(Duration.ofDays(7));
		PasswordResetToken oldCode = resetToken(Duration.ofDays(-2));
		PasswordResetToken freshCode = resetToken(Duration.ofMinutes(10));

		CleanupService.Result result = service.cleanUp();
		entityManager.clear();

		assertThat(result.refreshTokens()).isGreaterThanOrEqualTo(1);
		assertThat(result.resetTokens()).isGreaterThanOrEqualTo(1);
		assertThat(refreshTokens.existsById(longExpired.getId())).isFalse();
		assertThat(refreshTokens.existsById(justExpired.getId())).isTrue();
		assertThat(refreshTokens.existsById(active.getId())).isTrue();
		assertThat(resetTokens.existsById(oldCode.getId())).isFalse();
		assertThat(resetTokens.existsById(freshCode.getId())).isTrue();
	}

	@Test
	@DisplayName("Xoá phiên học rỗng đã cũ, giữ phiên rỗng mới và phiên có giờ học")
	void removesOnlyOldEmptySessions() {
		StudySession oldEmpty = session(Duration.ofDays(40), 0);
		StudySession recentEmpty = session(Duration.ofDays(1), 0);
		StudySession oldStudied = session(Duration.ofDays(40), 90);

		CleanupService.Result result = service.cleanUp();
		entityManager.clear();

		assertThat(result.emptySessions()).isGreaterThanOrEqualTo(1);
		assertThat(sessions.existsById(oldEmpty.getId())).isFalse();
		assertThat(sessions.existsById(recentEmpty.getId())).isTrue();
		assertThat(sessions.existsById(oldStudied.getId())).isTrue();
	}

	@Test
	@DisplayName("Xoá lượt nói IN_PROGRESS bị bỏ dở quá 24 giờ, giữ lượt mới và lượt đã chấm")
	void removesOnlyStaleInProgressSpeakingAttempts() {
		String tag = "C" + UUID.randomUUID().toString().replace("-", "");
		Topic topic = new Topic();
		topic.setSlug(tag);
		topic.setNameVi("Chủ đề " + tag);
		topic.setNameEn("Topic " + tag);
		topics.save(topic);
		SpeakingLesson lesson = new SpeakingLesson();
		lesson.setTopic(topic);
		lesson.setTitleVi("Nói " + tag);
		lesson.setTitleEn("Speak " + tag);
		lesson.setLevel(Level.BEGINNER);
		speakingLessons.saveAndFlush(lesson);

		SpeakingAttempt stale = speakingAttempt(lesson, SpeakingAttemptStatus.IN_PROGRESS, Duration.ofHours(30));
		SpeakingAttempt fresh = speakingAttempt(lesson, SpeakingAttemptStatus.IN_PROGRESS, Duration.ofHours(2));
		SpeakingAttempt failed = speakingAttempt(lesson, SpeakingAttemptStatus.FAILED, Duration.ofHours(30));

		CleanupService.Result result = service.cleanUp();
		entityManager.clear();

		assertThat(result.staleSpeakingAttempts()).isGreaterThanOrEqualTo(1);
		assertThat(speakingAttempts.existsById(stale.getId())).isFalse();
		assertThat(speakingAttempts.existsById(fresh.getId())).isTrue();
		assertThat(speakingAttempts.existsById(failed.getId())).isTrue();
	}

	private SpeakingAttempt speakingAttempt(SpeakingLesson lesson, SpeakingAttemptStatus status, Duration idle) {
		SpeakingAttempt attempt = new SpeakingAttempt();
		attempt.setUser(user);
		attempt.setLesson(lesson);
		attempt.setStatus(status);
		attempt.setStartedAt(now.minus(idle));
		attempt.setSubmittedAt(now.minus(idle));
		speakingAttempts.saveAndFlush(attempt);
		// updated_at do @UpdateTimestamp luôn là "bây giờ", nên đặt lùi bằng SQL.
		jdbc.update("update speaking_attempts set updated_at = ? where id = ?",
				java.sql.Timestamp.from(now.minus(idle)), uuidBytes(attempt.getId()));
		entityManager.clear();
		return attempt;
	}

	private static byte[] uuidBytes(UUID id) {
		java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(16);
		buffer.putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits());
		return buffer.array();
	}
}
