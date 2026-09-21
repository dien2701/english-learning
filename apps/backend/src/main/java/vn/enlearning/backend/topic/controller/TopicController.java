package vn.enlearning.backend.topic.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.topic.dto.TopicResponse;
import vn.enlearning.backend.topic.service.TopicService;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

	private final TopicService topicService;

	@GetMapping
	ApiResponse<List<TopicResponse>> list() {
		return ApiResponse.ok(topicService.list());
	}
}
