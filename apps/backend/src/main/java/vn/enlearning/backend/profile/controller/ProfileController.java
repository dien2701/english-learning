package vn.enlearning.backend.profile.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.dto.SimpleResponses.Message;
import vn.enlearning.backend.auth.dto.UserResponse;
import vn.enlearning.backend.auth.service.ClientInfo;
import vn.enlearning.backend.auth.service.RefreshCookieFactory;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.profile.dto.ChangePasswordRequest;
import vn.enlearning.backend.profile.dto.ProfileResponse;
import vn.enlearning.backend.profile.dto.SettingsRequest;
import vn.enlearning.backend.profile.dto.SettingsResponse;
import vn.enlearning.backend.profile.dto.UpdateProfileRequest;
import vn.enlearning.backend.profile.service.ProfileService;

@RestController
@RequiredArgsConstructor
public class ProfileController {

	private static final String PASSWORD_CHANGED_MESSAGE = "Đã đổi mật khẩu thành công.";

	private final ProfileService profileService;
	private final RefreshCookieFactory cookies;

	@GetMapping("/profile")
	ApiResponse<ProfileResponse> profile(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(profileService.get(userId(jwt)));
	}

	@PatchMapping("/profile")
	ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal Jwt jwt,
			@RequestBody UpdateProfileRequest request) {
		return ApiResponse.ok(profileService.update(userId(jwt), request));
	}

	/** Thu hồi mọi phiên cũ và đặt cookie refresh mới để thiết bị đang dùng không bị đăng xuất. */
	@PostMapping("/profile/password")
	ResponseEntity<ApiResponse<Message>> changePassword(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest http) {
		String refreshToken = profileService.changePassword(userId(jwt), request, ClientInfo.from(http));
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookies.issue(refreshToken).toString())
				.body(ApiResponse.ok(new Message(PASSWORD_CHANGED_MESSAGE)));
	}

	@GetMapping("/settings")
	ApiResponse<SettingsResponse> settings(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(profileService.getSettings(userId(jwt)));
	}

	@PatchMapping("/settings")
	ApiResponse<SettingsResponse> updateSettings(@AuthenticationPrincipal Jwt jwt,
			@RequestBody SettingsRequest request) {
		return ApiResponse.ok(profileService.updateSettings(userId(jwt), request));
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
