package vn.enlearning.backend.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.enlearning.backend.entity.UserSetting;

public interface UserSettingRepository extends JpaRepository<UserSetting, UUID> {

	Optional<UserSetting> findByUserId(UUID userId);

	/** Cài đặt của mọi tài khoản đang hoạt động đã bật nhắc học, kèm người dùng bằng một câu. */
	@Query("select s from UserSetting s join fetch s.user u where s.emailReminders = true "
			+ "and u.status = vn.enlearning.backend.entity.enums.AccountStatus.ACTIVE")
	List<UserSetting> findRemindable();
}
