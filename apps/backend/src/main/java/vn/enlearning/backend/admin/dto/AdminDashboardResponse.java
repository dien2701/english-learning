package vn.enlearning.backend.admin.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import vn.enlearning.backend.common.L10n;

/** Khớp {@code AdminDashboardData} ở frontend. Mọi số liệu tính trực tiếp từ DB; số theo thời gian tính trong 30 ngày gần nhất. */
public record AdminDashboardResponse(Overview overview, List<SignupPoint> signups,
		List<ContentCount> contentCounts, SystemStats system, List<Activity> activities) {

	/** {@code totalContent} gồm cả nội dung INACTIVE. */
	public record Overview(long totalUsers, long newUsers, long activeUsers, long studySessions, long totalContent) {
	}

	public record SignupPoint(L10n label, long count) {
	}

	public record ContentCount(ContentType skill, long count) {
	}

	/** Email tính theo {@code email_logs} trong 30 ngày; người dùng tính toàn hệ thống. */
	public record SystemStats(long admins, long lockedUsers, long pendingUsers, long emailsSent, long emailsFailed) {
	}

	/** Chưa có nguồn nhật ký hoạt động nên danh sách luôn rỗng; giữ trường để giao diện không phải đổi. */
	public record Activity(UUID id, String actor, L10n action, Instant occurredAt) {
	}
}
