package vn.enlearning.backend.cleanup;

import java.time.Clock;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.auth.repository.PasswordResetTokenRepository;
import vn.enlearning.backend.auth.repository.RefreshTokenRepository;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/** Dọn dữ liệu tạm định kỳ để các bảng token và phiên học không phình mãi. */
@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

	/** Số dòng đã xoá ở mỗi bảng. */
	public record Result(int refreshTokens, int resetTokens, int emptySessions) {
	}

	private final RefreshTokenRepository refreshTokens;
	private final PasswordResetTokenRepository resetTokens;
	private final StudySessionRepository studySessions;
	private final CleanupProperties properties;
	private final Clock clock;

	/** Chạy mỗi giờ, ở phút 15. */
	@Scheduled(cron = "0 15 * * * *")
	@Transactional
	public Result cleanUp() {
		Instant now = clock.instant();
		Instant tokenCutoff = now.minus(properties.tokenRetention());
		Result result = new Result(
				refreshTokens.deleteExpiredBefore(tokenCutoff),
				resetTokens.deleteExpiredBefore(tokenCutoff),
				studySessions.deleteEmptyBefore(now.minus(properties.emptySessionRetention())));
		if (result.refreshTokens() + result.resetTokens() + result.emptySessions() > 0) {
			log.info("Dọn dẹp: {} refresh token, {} mã OTP, {} phiên học rỗng", result.refreshTokens(),
					result.resetTokens(), result.emptySessions());
		}
		return result;
	}
}
