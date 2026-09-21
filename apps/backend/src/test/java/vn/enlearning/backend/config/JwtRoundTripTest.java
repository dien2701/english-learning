package vn.enlearning.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.security.JwtService;

/** Token do {@link JwtService} ký phải được {@link JwtConfig} đọc lại đúng, và bị từ chối khi sai khoá, sai issuer hay hết hạn. */
class JwtRoundTripTest {

	private static final String SECRET = "khoa-jwt-chi-dung-cho-test-0123456789abcdef0123456789abcdef";
	private static final String OTHER_SECRET = "mot-khoa-khac-hoan-toan-0123456789abcdef0123456789abcdef";
	private static final Instant LONG_AGO = Instant.parse("2020-01-01T00:00:00Z");

	private final JwtConfig config = new JwtConfig();

	private static SecurityProperties properties(String secret, String issuer) {
		return new SecurityProperties(secret, issuer, Duration.ofMinutes(15), Duration.ofDays(7), Duration.ofSeconds(10),
				false, "khoa-otp-chi-dung-cho-test-0123456789abcdef0123456789abcdef", Duration.ofMinutes(10), 5,
				Duration.ofSeconds(60));
	}

	private JwtService service(SecurityProperties properties, Instant now) {
		return new JwtService(config.jwtEncoder(properties), properties, Clock.fixed(now, ZoneOffset.UTC));
	}

	private static User user(Role role) {
		User user = new User();
		user.setId(UUID.randomUUID());
		user.setRole(role);
		return user;
	}

	@Test
	@DisplayName("Token mang id và vai trò, đọc lại đúng, không chứa thông tin định danh khác")
	void tokenCarriesSubjectAndRole() {
		SecurityProperties properties = properties(SECRET, "en-learning");
		User admin = user(Role.ADMIN);

		Jwt jwt = config.jwtDecoder(properties).decode(service(properties, Instant.now()).issueAccessToken(admin));

		assertThat(jwt.getSubject()).isEqualTo(admin.getId().toString());
		assertThat(jwt.getClaimAsString(JwtService.ROLE_CLAIM)).isEqualTo("ADMIN");
		// Issuer là chuỗi thường (StringOrURI theo RFC 7519), không phải URL nên đọc bằng getClaimAsString.
		assertThat(jwt.getClaimAsString("iss")).isEqualTo("en-learning");
		assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(15));
		assertThat(jwt.getClaims()).doesNotContainKeys("email", "name", "passwordHash");
	}

	@Test
	@DisplayName("Token hết hạn bị từ chối")
	void expiredTokenIsRejected() {
		SecurityProperties properties = properties(SECRET, "en-learning");
		String token = service(properties, LONG_AGO).issueAccessToken(user(Role.USER));

		assertThatThrownBy(() -> config.jwtDecoder(properties).decode(token)).isInstanceOf(JwtValidationException.class);
	}

	@Test
	@DisplayName("Token ký bằng khoá khác bị từ chối")
	void tokenSignedWithAnotherKeyIsRejected() {
		String forged = service(properties(OTHER_SECRET, "en-learning"), Instant.now()).issueAccessToken(user(Role.ADMIN));

		assertThatThrownBy(() -> config.jwtDecoder(properties(SECRET, "en-learning")).decode(forged))
				.isInstanceOf(BadJwtException.class);
	}

	@Test
	@DisplayName("Token của issuer khác bị từ chối dù cùng khoá")
	void tokenFromAnotherIssuerIsRejected() {
		String foreign = service(properties(SECRET, "he-thong-khac"), Instant.now()).issueAccessToken(user(Role.USER));

		assertThatThrownBy(() -> config.jwtDecoder(properties(SECRET, "en-learning")).decode(foreign))
				.isInstanceOf(JwtValidationException.class);
	}
}
