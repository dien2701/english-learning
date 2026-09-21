package vn.enlearning.backend.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

	/** Mã còn mở gần nhất của người dùng (chưa dùng, chưa bị vô hiệu). */
	Optional<PasswordResetToken> findFirstByUserIdAndUsedAtIsNullOrderByCreatedAtDesc(UUID userId);

	/** Mã gần nhất dù trạng thái nào, để tính khoảng chờ giữa hai lần xin mã. */
	Optional<PasswordResetToken> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);

	/** Đóng mọi mã còn mở của người dùng: mã mới vô hiệu mã cũ, và mã đã dùng xong thì không dùng lại. */
	@Modifying(flushAutomatically = true)
	@Query("update PasswordResetToken t set t.usedAt = :now where t.user.id = :userId and t.usedAt is null")
	int closeOpenByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

	/**
	 * Tăng nguyên tử bằng UPDATE, không đọc-sửa-ghi, để hai request song song không đếm hụt lần thử.
	 * {@code clearAutomatically}: bản {@code attempts} đang nằm trong bộ nhớ đã cũ nên phải bỏ đi, nếu không lần
	 * đọc tiếp theo trong cùng persistence context vẫn thấy số cũ.
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update PasswordResetToken t set t.attempts = t.attempts + 1 where t.id = :id")
	int incrementAttempts(@Param("id") UUID id);
}
