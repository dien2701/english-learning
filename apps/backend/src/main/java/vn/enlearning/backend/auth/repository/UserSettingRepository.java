package vn.enlearning.backend.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.UserSetting;

public interface UserSettingRepository extends JpaRepository<UserSetting, UUID> {

	Optional<UserSetting> findByUserId(UUID userId);
}
