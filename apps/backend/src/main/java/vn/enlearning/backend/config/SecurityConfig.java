package vn.enlearning.backend.config;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import vn.enlearning.backend.security.JwtService;
import vn.enlearning.backend.security.RestAccessDeniedHandler;
import vn.enlearning.backend.security.RestAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

	/**
	 * Endpoint công khai của module auth, dạng "METHOD /đường-dẫn" (không gồm context-path).
	 * Frontend gắn Bearer token vào mọi request; nếu token cũ đã hết hạn thì ngay cả đăng nhập
	 * cũng bị từ chối, nên các endpoint này bỏ qua Authorization thay vì kiểm nó.
	 */
	private static final Set<String> PUBLIC_AUTH_ENDPOINTS = Set.of(
			"POST /auth/register",
			"POST /auth/login",
			"POST /auth/refresh",
			"DELETE /auth/session",
			"POST /auth/forgot-password",
			"POST /auth/reset-password",
			"GET /auth/check-email");

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, RestAuthenticationEntryPoint entryPoint,
			RestAccessDeniedHandler accessDeniedHandler) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(e -> e
						.authenticationEntryPoint(entryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.oauth2ResourceServer(o -> o
						.bearerTokenResolver(bearerTokenResolver())
						.authenticationEntryPoint(entryPoint)
						.accessDeniedHandler(accessDeniedHandler)
						.jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/actuator/health/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/refresh",
								"/auth/forgot-password", "/auth/reset-password").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/auth/session").permitAll()
						.requestMatchers(HttpMethod.GET, "/auth/check-email").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated());
		return http.build();
	}

	private static BearerTokenResolver bearerTokenResolver() {
		DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
		return request -> isPublicAuthEndpoint(request) ? null : delegate.resolve(request);
	}

	private static boolean isPublicAuthEndpoint(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		return PUBLIC_AUTH_ENDPOINTS.contains(request.getMethod() + " " + path);
	}

	/** Quyền lấy từ claim {@code role} (USER, ADMIN) thành {@code ROLE_USER}, {@code ROLE_ADMIN}. */
	private static JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::authorities);
		return converter;
	}

	private static List<GrantedAuthority> authorities(Jwt jwt) {
		String role = jwt.getClaimAsString(JwtService.ROLE_CLAIM);
		return role == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		// Refresh token nằm trong cookie HttpOnly nên phải cho phép gửi kèm credentials.
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
