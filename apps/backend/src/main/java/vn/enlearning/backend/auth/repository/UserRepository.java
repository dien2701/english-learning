package vn.enlearning.backend.auth.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

	/** Chỉ thấy tài khoản chưa xoá mềm ({@code @SQLRestriction} của {@link User}). */
	Optional<User> findByEmail(String email);

	/**
	 * Query native để bỏ qua {@code @SQLRestriction}: email của tài khoản đã xoá mềm vẫn nằm trong
	 * unique index, nên vẫn tính là đã được dùng.
	 */
	@Query(value = "SELECT COUNT(*) FROM users WHERE email = :email", nativeQuery = true)
	long countByEmailIncludingDeleted(@Param("email") String email);

	default boolean existsByEmailIncludingDeleted(String email) {
		return countByEmailIncludingDeleted(email) > 0;
	}

	@Query(value = "SELECT COUNT(*) FROM users", nativeQuery = true)
	long countIncludingDeleted();

	long countByStatusAndLastActiveAtGreaterThanEqual(AccountStatus status, Instant since);

	/** Thời điểm đăng ký của người dùng từ mốc {@code from}, để gom theo tháng ở Service (múi giờ do Service chọn). */
	@Query("select u.createdAt from User u where u.createdAt >= :from")
	List<Instant> createdAtSince(@Param("from") Instant from);

	/** Người nhận thông báo: chỉ tài khoản ACTIVE; khoá và PENDING không bao giờ nhận. */
	@Query("select u.id from User u where u.status = vn.enlearning.backend.entity.enums.AccountStatus.ACTIVE")
	List<UUID> findActiveIds();

	@Query("select u.id from User u where u.status = vn.enlearning.backend.entity.enums.AccountStatus.ACTIVE "
			+ "and u.lastActiveAt >= :since")
	List<UUID> findRecentlyActiveIds(@Param("since") Instant since);

	@Query("select u.id from User u where u.status = vn.enlearning.backend.entity.enums.AccountStatus.ACTIVE "
			+ "and (u.lastActiveAt is null or u.lastActiveAt < :since)")
	List<UUID> findDormantIds(@Param("since") Instant since);

	@Query("select u.id from User u where u.status = vn.enlearning.backend.entity.enums.AccountStatus.ACTIVE "
			+ "and u.role = vn.enlearning.backend.entity.enums.Role.ADMIN")
	List<UUID> findActiveAdminIds();
}
