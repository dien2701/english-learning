package vn.enlearning.backend.security;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.config.SecurityProperties;
import vn.enlearning.backend.entity.User;

/**
 * Phát hành access token. Token không mang email hay tên: chỉ {@code sub} (id người dùng) và {@code role},
 * đủ để phân quyền mà không phải truy vấn DB ở mỗi request.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

	public static final String ROLE_CLAIM = "role";

	private final JwtEncoder encoder;
	private final SecurityProperties properties;
	private final Clock clock;

	public String issueAccessToken(User user) {
		Instant now = clock.instant();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.jwtIssuer())
				.subject(user.getId().toString())
				.issuedAt(now)
				.expiresAt(now.plus(properties.accessTokenTtl()))
				.id(UUID.randomUUID().toString())
				.claim(ROLE_CLAIM, user.getRole().name())
				.build();
		// Không khai header thì Nimbus mặc định RS256 và từ chối khoá đối xứng.
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
