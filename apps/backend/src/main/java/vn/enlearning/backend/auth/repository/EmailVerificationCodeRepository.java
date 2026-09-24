package vn.enlearning.backend.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.EmailVerificationCode;

public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, UUID> {

	Optional<EmailVerificationCode> findFirstByEmailAndUsedAtIsNullOrderByCreatedAtDesc(String email);

	Optional<EmailVerificationCode> findFirstByEmailOrderByCreatedAtDesc(String email);

	@Modifying(flushAutomatically = true)
	@Query("update EmailVerificationCode c set c.usedAt = :now where c.email = :email and c.usedAt is null")
	int closeOpenByEmail(@Param("email") String email, @Param("now") Instant now);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update EmailVerificationCode c set c.attempts = c.attempts + 1 where c.id = :id")
	int incrementAttempts(@Param("id") UUID id);

	@Modifying
	@Query("delete from EmailVerificationCode c where c.expiresAt < :cutoff")
	int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
