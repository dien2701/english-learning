package vn.enlearning.backend.auth.service;

import java.time.Clock;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.auth.dto.Emails;
import vn.enlearning.backend.auth.dto.LoginRequest;
import vn.enlearning.backend.auth.dto.RegisterRequest;
import vn.enlearning.backend.auth.dto.UserResponse;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.auth.validation.PasswordPolicy;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.security.JwtService;

/** Đăng ký, đăng nhập, làm mới phiên và tra cứu tài khoản. */
@Service
public class AuthService {

	private final UserRepository users;
	private final UserSettingRepository settings;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokens;
	private final Clock clock;

	/**
	 * Băm giả để đăng nhập email không tồn tại cũng tốn đúng một lần BCrypt; nếu không, thời gian phản hồi
	 * cho biết email nào có tài khoản.
	 */
	private final String dummyHash;

	public AuthService(UserRepository users, UserSettingRepository settings, PasswordEncoder passwordEncoder,
			JwtService jwtService, RefreshTokenService refreshTokens, Clock clock) {
		this.users = users;
		this.settings = settings;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokens = refreshTokens;
		this.clock = clock;
		this.dummyHash = passwordEncoder.encode("mat-khau-gia-de-can-bang-thoi-gian");
	}

	/** Phiên vừa tạo: access token và người dùng đi trong thân phản hồi, refresh token đi trong cookie. */
	public record Session(String accessToken, UserResponse user, String refreshToken) {
	}

	@Transactional
	public Session register(RegisterRequest request, ClientInfo client) {
		if (users.existsByEmailIncludingDeleted(request.email())) {
			throw emailTaken();
		}

		User user = new User();
		user.setEmail(request.email());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setFullName(request.fullName());
		user.setLastActiveAt(clock.instant());
		try {
			users.saveAndFlush(user);
		} catch (DataIntegrityViolationException e) {
			// Hai request đăng ký cùng email chạy song song: unique index chặn request đến sau.
			throw emailTaken();
		}

		UserSetting setting = new UserSetting();
		setting.setUser(user);
		settings.save(setting);

		return startSession(user, client);
	}

	@Transactional
	public Session login(LoginRequest request, ClientInfo client) {
		User user = users.findByEmail(request.email()).orElse(null);
		String hash = user == null ? dummyHash : user.getPasswordHash();
		// BCrypt từ chối mật khẩu quá 72 byte; mật khẩu như vậy không thể là mật khẩu đã lưu.
		boolean matches = !PasswordPolicy.exceedsBcryptLimit(request.password())
				&& passwordEncoder.matches(request.password(), hash);

		// Sai mật khẩu và không có tài khoản cùng một lỗi để không lộ email nào đã đăng ký.
		if (user == null || !matches) {
			throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
		}
		// Chỉ nói "bị khoá" sau khi đã chứng minh biết mật khẩu.
		if (user.getStatus() != AccountStatus.ACTIVE) {
			throw new ApiException(ErrorCode.ACCOUNT_LOCKED);
		}

		user.setLastActiveAt(clock.instant());
		return startSession(user, client);
	}

	/** {@code noRollbackFor}: phát hiện dùng lại token phải ghi lại việc thu hồi phiên dù trả 401. */
	@Transactional(noRollbackFor = ApiException.class)
	public Session refresh(String rawRefreshToken, ClientInfo client) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new ApiException(ErrorCode.UNAUTHORIZED);
		}
		RefreshTokenService.Rotation rotation = refreshTokens.rotate(rawRefreshToken, client);
		User user = rotation.user();
		user.setLastActiveAt(clock.instant());
		return new Session(jwtService.issueAccessToken(user), UserResponse.from(user), rotation.rawToken());
	}

	public void logout(String rawRefreshToken) {
		refreshTokens.revoke(rawRefreshToken);
	}

	@Transactional(readOnly = true)
	public UserResponse me(UUID userId) {
		User user = users.findById(userId)
				.filter(found -> !found.isDeleted())
				.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
		// Access token còn sống tối đa vài phút sau khi bị khoá; đây là chỗ chặn sớm cho lần tải lại trang.
		if (user.getStatus() != AccountStatus.ACTIVE) {
			throw new ApiException(ErrorCode.ACCOUNT_LOCKED);
		}
		return UserResponse.from(user);
	}

	@Transactional(readOnly = true)
	public boolean isEmailAvailable(String rawEmail) {
		String email = Emails.normalize(rawEmail);
		if (!Emails.isValid(email)) {
			throw ApiException.field(ErrorCode.VALIDATION, "email", "auth.validation.emailFormat");
		}
		return !users.existsByEmailIncludingDeleted(email);
	}

	private Session startSession(User user, ClientInfo client) {
		String refreshToken = refreshTokens.issue(user, client);
		return new Session(jwtService.issueAccessToken(user), UserResponse.from(user), refreshToken);
	}

	private static ApiException emailTaken() {
		return ApiException.field(ErrorCode.EMAIL_TAKEN, "email", "errors.emailTaken");
	}
}
