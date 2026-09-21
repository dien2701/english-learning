package vn.enlearning.backend.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

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
}
