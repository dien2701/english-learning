package vn.enlearning.backend.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;

/** Khớp {@code AdminDashboardData} ở frontend. */
public record AdminDashboardResponse(Overview overview, List<SignupPoint> signups, List<ContentCount> contentCounts,
		List<Activity> activities) {

	/** {@code studySessions}: số phiên học trong 30 ngày gần nhất; {@code totalContent} gồm cả nội dung INACTIVE. */
	public record Overview(long totalUsers, long activeUsers, long studySessions, long totalContent) {
	}

	public record SignupPoint(L10n label, long count) {
	}

	public record ContentCount(ContentType skill, long count) {
	}

	/** Chưa có nguồn nhật ký hoạt động nên danh sách luôn rỗng; giữ trường để giao diện không phải đổi. */
	public record Activity(UUID id, String actor, L10n action, Instant occurredAt) {
	}
}
