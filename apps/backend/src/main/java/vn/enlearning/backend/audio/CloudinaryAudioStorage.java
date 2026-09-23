package vn.enlearning.backend.audio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Lưu audio lên Cloudinary (resource_type {@code video}, thư mục Media Library {@value #FOLDER}) bằng REST có ký SHA-1,
 * không kéo thêm SDK. Chỉ nạp khi có {@code app.audio.cloudinary-url}. Log không in key, secret hay thân phản hồi.
 */
@Slf4j
@Component
@ConditionalOnExpression("!'${app.audio.cloudinary-url:}'.isBlank()")
class CloudinaryAudioStorage implements AudioStorage {

	/** Thư mục Home/En-Learning trong Media Library (chế độ thư mục động cần {@code asset_folder}). */
	static final String FOLDER = "En-Learning";
	/** Tiền tố public_id của audio tải lên trước khi đổi thư mục; vẫn được phép xoá. */
	private static final String LEGACY_PREFIX = "en-learning/listening/";
	private static final Pattern CLOUDINARY_URL = Pattern.compile("^cloudinary://([^:@/\\s]+):([^@/\\s]+)@([^/?#\\s]+)");

	private final String apiKey;
	private final String apiSecret;
	private final String cloudName;
	private final JsonMapper jsonMapper;
	private final RestClient http;

	CloudinaryAudioStorage(AudioProperties properties, JsonMapper jsonMapper) {
		Matcher m = CLOUDINARY_URL.matcher(properties.cloudinaryUrl().trim());
		if (!m.find()) {
			// Không đưa giá trị vào thông báo: nó chứa secret.
			throw new IllegalStateException("CLOUDINARY_URL phải có dạng cloudinary://<api_key>:<api_secret>@<cloud_name>");
		}
		this.apiKey = m.group(1);
		this.apiSecret = m.group(2);
		this.cloudName = m.group(3);
		this.jsonMapper = jsonMapper;
		this.http = AudioHttp.client(properties.cloudinaryBaseUrl(), properties.cloudinaryTimeout());
	}

	@Override
	public StoredAudio store(byte[] data, String name, String extension) {
		String publicId = FOLDER + "/" + name;
		String fileName = name + "." + extension;
		Map<String, String> signed = new TreeMap<>(
				Map.of("public_id", publicId, "asset_folder", FOLDER, "timestamp", timestamp()));

		MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
		form.add("file", new ByteArrayResource(data) {
			@Override
			public String getFilename() {
				return fileName;
			}
		});
		signed.forEach(form::add);
		form.add("api_key", apiKey);
		form.add("signature", sign(signed));
		try {
			String body = http.post()
					.uri("/v1_1/{cloud}/video/upload", cloudName)
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(form)
					.retrieve()
					.body(String.class);
			JsonNode json = jsonMapper.readTree(body);
			String url = json.path("secure_url").asString("");
			if (url.isEmpty()) {
				throw new AudioStorageException("Cloudinary không trả secure_url");
			}
			return new StoredAudio(url, json.path("public_id").asString(publicId));
		} catch (RestClientResponseException e) {
			// Không nối cause: thông báo của nó chứa thân phản hồi.
			log.warn("Cloudinary trả HTTP {} khi tải lên", e.getStatusCode().value());
			throw new AudioStorageException("Cloudinary trả HTTP " + e.getStatusCode().value());
		} catch (RestClientException | JacksonException e) {
			log.warn("Không tải lên được Cloudinary: {}", e.getClass().getSimpleName());
			throw new AudioStorageException("Không tải lên được Cloudinary: " + e.getClass().getSimpleName(), e);
		}
	}

	@Override
	public void delete(String publicId) {
		// Chỉ xoá file do ứng dụng tạo; audio cục bộ cũ (khi chưa có Cloudinary) không có tiền tố này.
		if (publicId == null || !(publicId.startsWith(FOLDER + "/") || publicId.startsWith(LEGACY_PREFIX))) {
			return;
		}
		Map<String, String> signed = new TreeMap<>(Map.of("public_id", publicId, "timestamp", timestamp()));
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		signed.forEach(form::add);
		form.add("api_key", apiKey);
		form.add("signature", sign(signed));
		try {
			http.post()
					.uri("/v1_1/{cloud}/video/destroy", cloudName)
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(form)
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException e) {
			log.warn("Cloudinary trả HTTP {} khi xoá {}", e.getStatusCode().value(), publicId);
		} catch (RestClientException e) {
			log.warn("Không xoá được {} trên Cloudinary: {}", publicId, e.getClass().getSimpleName());
		}
	}

	private static String timestamp() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}

	/** SHA-1 hex của {@code k=v&k=v} (khoá xếp theo thứ tự chữ cái) nối với API secret, theo tài liệu Cloudinary. */
	String sign(Map<String, String> params) {
		String toSign = new TreeMap<>(params).entrySet().stream()
				.map(e -> e.getKey() + "=" + e.getValue())
				.collect(Collectors.joining("&")) + apiSecret;
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(toSign.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
