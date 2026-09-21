package vn.enlearning.backend.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Sinh và băm token: mọi thứ nhạy cảm chỉ được lưu dưới dạng bản băm, không bao giờ lưu thô. */
public final class TokenHasher {

	private static final SecureRandom RANDOM = new SecureRandom();

	private TokenHasher() {
	}

	/** Chuỗi ngẫu nhiên an toàn cho URL/cookie, không có ký tự đệm. */
	public static String randomToken(int bytes) {
		byte[] buffer = new byte[bytes];
		RANDOM.nextBytes(buffer);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
	}

	/** Số nguyên ngẫu nhiên đều trong {@code [0, bound)}. */
	public static int randomInt(int bound) {
		return RANDOM.nextInt(bound);
	}

	public static String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("JVM thiếu SHA-256", e);
		}
	}

	/** HMAC-SHA256 dạng hex 64 ký tự, vừa cột {@code token_hash}. */
	public static String hmacSha256Hex(String secret, String message) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("JVM thiếu HmacSHA256", e);
		}
	}

	/** So sánh trong thời gian không phụ thuộc vị trí byte khác nhau đầu tiên. */
	public static boolean constantTimeEquals(String a, String b) {
		return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
	}
}
