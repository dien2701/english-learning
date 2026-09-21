package vn.enlearning.backend.admin.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminContentDetailResponse;
import vn.enlearning.backend.admin.dto.AdminContentRequest;
import vn.enlearning.backend.admin.dto.AdminContentSummaryResponse;
import vn.enlearning.backend.admin.dto.ContentStatusRequest;
import vn.enlearning.backend.admin.dto.ContentStatusResponse;
import vn.enlearning.backend.admin.service.AdminContentService;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.PageResponse;

/** Chỉ ROLE_ADMIN (khoá ở {@code SecurityConfig}: {@code /admin/**}). */
@RestController
@RequestMapping("/admin/content")
@RequiredArgsConstructor
public class AdminContentController {

	private final AdminContentService content;

	@GetMapping
	ApiResponse<PageResponse<AdminContentSummaryResponse>> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String skill,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String level,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize) {
		return ApiResponse.ok(content.list(search, skill, status, level, page, pageSize));
	}

	@GetMapping("/{id}")
	ApiResponse<AdminContentDetailResponse> detail(@PathVariable UUID id) {
		return ApiResponse.ok(content.get(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<AdminContentDetailResponse> create(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody AdminContentRequest request) {
		return ApiResponse.ok(content.create(UUID.fromString(jwt.getSubject()), request));
	}

	@PutMapping("/{id}")
	ApiResponse<AdminContentDetailResponse> update(@PathVariable UUID id,
			@Valid @RequestBody AdminContentRequest request) {
		return ApiResponse.ok(content.update(id, request));
	}

	/** Chuyển ACTIVE/INACTIVE: người học thấy hoặc không thấy nội dung ngay. */
	@PatchMapping("/{id}")
	ApiResponse<ContentStatusResponse> setStatus(@PathVariable UUID id,
			@Valid @RequestBody ContentStatusRequest request) {
		return ApiResponse.ok(content.setStatus(id, request.status()));
	}

	/** 409 nếu nội dung đã có trong lịch sử học; khi đó dùng PATCH sang INACTIVE. */
	@DeleteMapping("/{id}")
	ApiResponse<Map<String, Boolean>> delete(@PathVariable UUID id) {
		content.delete(id);
		return ApiResponse.ok(Map.of("deleted", true));
	}
}
