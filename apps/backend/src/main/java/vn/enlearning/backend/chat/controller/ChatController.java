package vn.enlearning.backend.chat.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.chat.dto.ChatConversationDetailResponse;
import vn.enlearning.backend.chat.dto.ChatConversationResponse;
import vn.enlearning.backend.chat.dto.ChatQuotaResponse;
import vn.enlearning.backend.chat.dto.RenameConversationRequest;
import vn.enlearning.backend.chat.dto.SendMessageRequest;
import vn.enlearning.backend.chat.dto.SendMessageResponse;
import vn.enlearning.backend.chat.service.ChatService;
import vn.enlearning.backend.common.ApiErrorResponse;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ApiResponse;
import vn.enlearning.backend.common.ErrorCode;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

	/** Câu hỏi gợi ý cho hội thoại còn trống. */
	private static final List<String> STARTERS = List.of(
			"Phân biệt \"make\" và \"do\" như thế nào?",
			"Làm sao để cải thiện kỹ năng nghe?",
			"Mở bài IELTS Writing Task 2 nên viết ra sao?",
			"Gợi ý cho mình bài luyện tập phù hợp với trình độ hiện tại");

	private static final long STREAM_TIMEOUT_MS = 120_000;

	private final ChatService chat;
	private final ExecutorService streams = Executors.newVirtualThreadPerTaskExecutor();

	@GetMapping("/conversations")
	ApiResponse<List<ChatConversationResponse>> list(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(chat.list(userId(jwt)));
	}

	@PostMapping("/conversations")
	ApiResponse<ChatConversationDetailResponse> create(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(chat.create(userId(jwt)));
	}

	@GetMapping("/conversations/{id}")
	ApiResponse<ChatConversationDetailResponse> detail(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		return ApiResponse.ok(chat.get(userId(jwt), id));
	}

	@PatchMapping("/conversations/{id}")
	ApiResponse<ChatConversationResponse> rename(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody RenameConversationRequest request) {
		return ApiResponse.ok(chat.rename(userId(jwt), id, request.title()));
	}

	@DeleteMapping("/conversations/{id}")
	ApiResponse<Map<String, Boolean>> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
		chat.delete(userId(jwt), id);
		return ApiResponse.ok(Map.of("deleted", true));
	}

	@PostMapping("/conversations/{id}/messages")
	ApiResponse<SendMessageResponse> send(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody SendMessageRequest request) {
		return ApiResponse.ok(chat.send(userId(jwt), id, request.content()));
	}

	/** Như gửi tin thường nhưng trả SSE: {@code delta} {"t":"..."} nhiều lần, rồi {@code done} hoặc {@code error}. */
	@PostMapping("/conversations/{id}/messages/stream")
	SseEmitter stream(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
			@Valid @RequestBody SendMessageRequest request) {
		UUID userId = userId(jwt);
		SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);
		streams.execute(() -> {
			try {
				SendMessageResponse done = chat.send(userId, id, request.content(), text -> {
					try {
						emitter.send(SseEmitter.event().name("delta").data(Map.of("t", text)));
					} catch (java.io.IOException e) {
						throw new java.io.UncheckedIOException(e);
					}
				});
				emitter.send(SseEmitter.event().name("done").data(done));
				emitter.complete();
			} catch (Exception e) {
				try {
					ErrorCode code = e instanceof ApiException api ? api.getCode() : ErrorCode.INTERNAL;
					emitter.send(SseEmitter.event().name("error").data(ApiErrorResponse.of(code)));
				} catch (java.io.IOException ignored) {
					// Client đã ngắt kết nối.
				}
				emitter.complete();
			}
		});
		return emitter;
	}

	@GetMapping("/quota")
	ApiResponse<ChatQuotaResponse> quota(@AuthenticationPrincipal Jwt jwt) {
		return ApiResponse.ok(chat.quota(userId(jwt)));
	}

	@GetMapping("/starters")
	ApiResponse<List<String>> starters() {
		return ApiResponse.ok(STARTERS);
	}

	private static UUID userId(Jwt jwt) {
		return UUID.fromString(jwt.getSubject());
	}
}
