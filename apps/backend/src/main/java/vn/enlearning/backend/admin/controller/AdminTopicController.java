package vn.enlearning.backend.admin.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminTopicResponse;
import vn.enlearning.backend.admin.dto.TopicRequest;
import vn.enlearning.backend.admin.service.AdminTopicService;
import vn.enlearning.backend.common.ApiResponse;

@RestController
@RequestMapping("/admin/topics")
@RequiredArgsConstructor
public class AdminTopicController {

	private final AdminTopicService topics;

	@GetMapping
	ApiResponse<List<AdminTopicResponse>> list() {
		return ApiResponse.ok(topics.list());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<AdminTopicResponse> create(@Valid @RequestBody TopicRequest request) {
		return ApiResponse.ok(topics.create(request));
	}

	@PutMapping("/{id}")
	ApiResponse<AdminTopicResponse> update(@PathVariable UUID id, @Valid @RequestBody TopicRequest request) {
		return ApiResponse.ok(topics.update(id, request));
	}

	/** 409 nếu chủ đề còn nội dung. */
	@DeleteMapping("/{id}")
	ApiResponse<Map<String, Boolean>> delete(@PathVariable UUID id) {
		topics.delete(id);
		return ApiResponse.ok(Map.of("deleted", true));
	}
}
