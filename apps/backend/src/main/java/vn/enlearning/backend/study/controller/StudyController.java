package vn.enlearning.backend.study.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.study.dto.HeartbeatRequest;
import vn.enlearning.backend.study.dto.HeartbeatResponse;
import vn.enlearning.backend.study.service.StudySessionService;

@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
public class StudyController {

	private final StudySessionService studySessions;

	@PostMapping("/heartbeat")
	ApiResponse<HeartbeatResponse> heartbeat(@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody HeartbeatRequest request) {
		return ApiResponse.ok(studySessions.heartbeat(UUID.fromString(jwt.getSubject()), request));
	}
}
