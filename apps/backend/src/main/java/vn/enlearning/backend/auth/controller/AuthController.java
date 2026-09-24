package vn.enlearning.backend.auth.controller;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.dto.AuthResponse;
import vn.enlearning.backend.auth.dto.ForgotPasswordRequest;
import vn.enlearning.backend.auth.dto.LoginRequest;
import vn.enlearning.backend.auth.dto.RegisterRequest;
import vn.enlearning.backend.auth.dto.ResetPasswordRequest;
import vn.enlearning.backend.auth.dto.SendVerificationCodeRequest;
import vn.enlearning.backend.auth.dto.SimpleResponses.EmailAvailability;
import vn.enlearning.backend.auth.dto.SimpleResponses.LoggedOut;
import vn.enlearning.backend.auth.dto.SimpleResponses.Message;
import vn.enlearning.backend.auth.dto.UserResponse;
import vn.enlearning.backend.auth.service.AuthService;
import vn.enlearning.backend.auth.service.ClientInfo;
import vn.enlearning.backend.auth.service.EmailVerificationService;
import vn.enlearning.backend.auth.service.PasswordResetService;
import vn.enlearning.backend.auth.service.RefreshCookieFactory;
import vn.enlearning.backend.common.ApiResponse;

/**
 * Các endpoint xác thực. Chỉ nhận request, gọi Service và gắn cookie; mọi luật nghiệp vụ nằm ở Service.
 * Hợp đồng bám {@code mocks/handlers/auth.ts} của frontend, trừ chỗ refresh token đi bằng cookie.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	/** Thông điệp cố định, cố ý không cho biết email có tài khoản hay không. */
	private static final String FORGOT_PASSWORD_MESSAGE =
			"Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu đã được gửi đi.";
	private static final String VERIFICATION_CODE_SENT_MESSAGE = "Mã xác minh đã được gửi tới email của bạn.";
	private static final String RESET_PASSWORD_MESSAGE =
			"Đặt lại mật khẩu thành công. Bạn có thể đăng nhập lại.";

	private final AuthService authService;
	private final PasswordResetService passwordResetService;
	private final EmailVerificationService emailVerificationService;
	private final RefreshCookieFactory cookies;

	/** Gửi mã OTP xác minh email; đăng ký chỉ thành công khi kèm đúng mã này. */
	@PostMapping("/register/send-code")
	ApiResponse<Message> sendRegisterCode(@Valid @RequestBody SendVerificationCodeRequest request) {
		emailVerificationService.sendCode(request.email(), request.language());
		return ApiResponse.ok(new Message(VERIFICATION_CODE_SENT_MESSAGE));
	}

	@PostMapping("/register")
	ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request,
			HttpServletRequest http) {
		return session(HttpStatus.CREATED, authService.register(request, ClientInfo.from(http)));
	}

	@PostMapping("/login")
	ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
		return session(HttpStatus.OK, authService.login(request, ClientInfo.from(http)));
	}

	/** Đổi refresh token trong cookie lấy access token mới, đồng thời xoay cookie. */
	@PostMapping("/refresh")
	ResponseEntity<ApiResponse<AuthResponse>> refresh(
			@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken,
			HttpServletRequest http) {
		return session(HttpStatus.OK, authService.refresh(refreshToken, ClientInfo.from(http)));
	}

	/** Đăng xuất. Dùng cookie làm bằng chứng nên vẫn chạy được khi access token đã hết hạn. */
	@DeleteMapping("/session")
	ResponseEntity<ApiResponse<LoggedOut>> logout(
			@CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
		authService.logout(refreshToken);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
				.body(ApiResponse.ok(new LoggedOut(true)));
	}

	@GetMapping("/me")
	ApiResponse<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(authService.me(UUID.fromString(jwt.getSubject())));
	}

	@GetMapping("/check-email")
	ApiResponse<EmailAvailability> checkEmail(@RequestParam(required = false) String email) {
		return ApiResponse.ok(new EmailAvailability(authService.isEmailAvailable(email)));
	}

	@PostMapping("/forgot-password")
	ApiResponse<Message> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		passwordResetService.requestCode(request.email());
		return ApiResponse.ok(new Message(FORGOT_PASSWORD_MESSAGE));
	}

	@PostMapping("/reset-password")
	ApiResponse<Message> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		passwordResetService.resetPassword(request);
		return ApiResponse.ok(new Message(RESET_PASSWORD_MESSAGE));
	}

	private ResponseEntity<ApiResponse<AuthResponse>> session(HttpStatus status, AuthService.Session session) {
		return ResponseEntity.status(status)
				.header(HttpHeaders.SET_COOKIE, cookies.issue(session.refreshToken()).toString())
				.body(ApiResponse.ok(new AuthResponse(session.accessToken(), session.user())));
	}
}
