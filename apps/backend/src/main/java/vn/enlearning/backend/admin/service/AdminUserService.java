package vn.enlearning.backend.admin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminUserResponse;
import vn.enlearning.backend.admin.dto.UpdateUserRequest;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.service.RefreshTokenService;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.IdCount;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository;

/**
 * Quản lý người dùng. Khoá tài khoản thu hồi mọi refresh token nên phiên không gia hạn được; access token đang
 * sống còn hiệu lực tối đa 15 phút (đã chốt: không tra trạng thái ở mỗi request). Admin không được tự đổi
 * trạng thái hay vai trò của chính mình để không tự khoá mình khỏi hệ thống.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final UserRepository users;
	private final RefreshTokenService refreshTokens;
	private final PracticeAttemptRepository attempts;
	private final WritingSubmissionRepository submissions;
	private final SpeakingAttemptRepository speakingAttempts;

	@Transactional(readOnly = true)
	public PageResponse<AdminUserResponse> list(String search, String role, String status, int page, int pageSize) {
		Role roleFilter = parse(Role.class, role);
		AccountStatus statusFilter = parse(AccountStatus.class, status);
		int size = Math.max(1, Math.min(pageSize, 100));
		int current = Math.max(1, page);

		Page<User> found = users.findAll(filter(search, roleFilter, statusFilter),
				PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"))));
		return new PageResponse<>(toResponses(found.getContent()), current, size, found.getTotalElements(),
				found.getTotalPages());
	}

	@Transactional(readOnly = true)
	public AdminUserResponse get(UUID id) {
		return toResponses(List.of(find(id))).get(0);
	}

	@Transactional
	public AdminUserResponse update(UUID adminId, UUID id, UpdateUserRequest request) {
		if (request.status() == null && request.role() == null) {
			throw ApiException.field(ErrorCode.VALIDATION, "status", "errors.field.nothingToUpdate");
		}
		if (request.status() == AccountStatus.PENDING) {
			throw ApiException.field(ErrorCode.VALIDATION, "status", "errors.field.statusInvalid");
		}
		User user = find(id);
		if (user.getId().equals(adminId)) {
			throw new ApiException(ErrorCode.INVALID_STATE);
		}
		if (request.role() != null) {
			user.setRole(request.role());
		}
		if (request.status() != null) {
			user.setStatus(request.status());
			if (request.status() == AccountStatus.LOCKED) {
				refreshTokens.revokeAll(user.getId());
			}
		}
		users.flush();
		return toResponses(List.of(user)).get(0);
	}

	private User find(UUID id) {
		return users.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	private static Specification<User> filter(String search, Role role, AccountStatus status) {
		return (root, query, cb) -> {
			List<Predicate> where = new ArrayList<>();
			if (role != null) {
				where.add(cb.equal(root.get("role"), role));
			}
			if (status != null) {
				where.add(cb.equal(root.get("status"), status));
			}
			if (search != null && !search.isBlank()) {
				String like = "%" + search.trim().toLowerCase(Locale.ROOT)
						.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
				where.add(cb.or(
						cb.like(cb.lower(root.get("fullName")), like, '\\'),
						cb.like(cb.lower(root.get("email")), like, '\\')));
			}
			return cb.and(where.toArray(Predicate[]::new));
		};
	}

	/** Số bài đã hoàn thành = lượt Nghe/Đọc/Kiểm tra đã nộp + bài Viết đã chấm + lượt Nói đã chấm; đếm gộp theo trang. */
	private List<AdminUserResponse> toResponses(List<User> page) {
		List<UUID> ids = page.stream().map(User::getId).toList();
		Map<UUID, Long> completed = new HashMap<>();
		if (!ids.isEmpty()) {
			for (List<IdCount> counts : List.of(attempts.completedByUsers(ids), submissions.gradedByUsers(ids),
					speakingAttempts.gradedByUsers(ids))) {
				counts.forEach(c -> completed.merge(c.getId(), c.getTotal(), Long::sum));
			}
		}
		return page.stream()
				.map(u -> new AdminUserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getStatus(),
						u.getCreatedAt(), u.getLastActiveAt(), completed.getOrDefault(u.getId(), 0L)))
				.toList();
	}

	private static <E extends Enum<E>> E parse(Class<E> type, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw ApiException.field(ErrorCode.VALIDATION, type.getSimpleName().toLowerCase(Locale.ROOT), "errors.badRequest");
		}
	}
}
