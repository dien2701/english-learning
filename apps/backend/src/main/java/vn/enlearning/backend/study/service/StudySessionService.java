package vn.enlearning.backend.study.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.study.dto.HeartbeatRequest;
import vn.enlearning.backend.study.dto.HeartbeatResponse;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/**
 * Heartbeat học tập. Mỗi lần cộng tối đa {@value #MAX_CREDIT_SECONDS} giây vào phiên đang mở; im lặng quá
 * 2 phút, hoặc đổi kỹ năng/nội dung, thì mở phiên mới (phiên mới bắt đầu ở 0 giây).
 */
@Service
@RequiredArgsConstructor
public class StudySessionService {

	static final int MAX_CREDIT_SECONDS = 60;
	static final Duration SILENCE_LIMIT = Duration.ofMinutes(2);

	private final StudySessionRepository sessions;
	private final UserRepository users;
	private final Clock clock;

	@Transactional
	public HeartbeatResponse heartbeat(UUID userId, HeartbeatRequest request) {
		Instant now = clock.instant();
		StudySession session = sessions.findFirstByUserIdOrderByLastHeartbeatAtDesc(userId)
				.filter(s -> s.getSkill() == request.skill()
						&& Objects.equals(s.getRefId(), request.refId())
						&& !now.isAfter(s.getLastHeartbeatAt().plus(SILENCE_LIMIT)))
				.orElse(null);

		if (session == null) {
			session = new StudySession();
			session.setUser(users.getReferenceById(userId));
			session.setSkill(request.skill());
			session.setRefId(request.refId());
			session.setStartedAt(now);
			session.setActiveSeconds(0);
		} else {
			long elapsed = Duration.between(session.getLastHeartbeatAt(), now).toSeconds();
			int credit = (int) Math.max(0, Math.min(MAX_CREDIT_SECONDS, elapsed));
			session.setActiveSeconds(session.getActiveSeconds() + credit);
		}
		session.setLastHeartbeatAt(now);
		sessions.save(session);
		return new HeartbeatResponse(session.getId(), session.getActiveSeconds());
	}
}
