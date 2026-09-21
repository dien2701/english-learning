package vn.enlearning.backend.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	/**
	 * Kèm luôn người dùng bằng một câu duy nhất, và loại token của tài khoản đã xoá mềm (nạp lười
	 * một User bị {@code @SQLRestriction} ẩn sẽ ném {@code EntityNotFoundException}).
	 */
	@Query("select t from RefreshToken t join fetch t.user u where t.tokenHash = :tokenHash and u.deletedAt is null")
	Optional<RefreshToken> findActiveOwnerByTokenHash(@Param("tokenHash") String tokenHash);

	/**
	 * Thu hồi một token nếu nó còn hiệu lực. Trả về 0 khi đã bị thu hồi từ trước, ví dụ một tab khác vừa
	 * xoay chính token này: một câu UPDATE có điều kiện nên chỉ đúng một request thắng.
	 */
	@Modifying(flushAutomatically = true)
	@Query("update RefreshToken t set t.revokedAt = :now where t.id = :id and t.revokedAt is null")
	int revokeIfActive(@Param("id") UUID id, @Param("now") Instant now);

	@Modifying(flushAutomatically = true)
	@Query("update RefreshToken t set t.revokedAt = :now where t.user.id = :userId and t.revokedAt is null")
	int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

	/** Xoá token đã hết hạn trước {@code cutoff}. Token bị thu hồi nhưng chưa hết hạn phải giữ để nhận ra dùng lại. */
	@Modifying
	@Query("delete from RefreshToken t where t.expiresAt < :cutoff")
	int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
