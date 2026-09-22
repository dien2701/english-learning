package vn.enlearning.backend.audio.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.audio.dto.ListeningAudioResponse;
import vn.enlearning.backend.audio.service.ListeningAudioService;
import vn.enlearning.backend.common.ApiResponse;

/** Chỉ ROLE_ADMIN (khoá ở {@code SecurityConfig}: {@code /admin/**}). */
@RestController
@RequestMapping("/admin/listening/{id}/audio")
@RequiredArgsConstructor
public class AdminListeningAudioController {

	private final ListeningAudioService audio;

	/** Sinh MP3 đa giọng từ transcript và thay audio hiện có. Có thể mất vài chục giây với bài dài. */
	@PostMapping("/generate")
	ApiResponse<ListeningAudioResponse> generate(@PathVariable UUID id) {
		return ApiResponse.ok(audio.generate(id));
	}

	/** Tải file audio lên (multipart, trường {@code file}): mp3, m4a hoặc wav, tối đa 20MB. */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ApiResponse<ListeningAudioResponse> upload(@PathVariable UUID id,
			@RequestParam(value = "file", required = false) MultipartFile file) {
		return ApiResponse.ok(audio.upload(id, file));
	}

	@DeleteMapping
	ApiResponse<ListeningAudioResponse> remove(@PathVariable UUID id) {
		return ApiResponse.ok(audio.remove(id));
	}
}
