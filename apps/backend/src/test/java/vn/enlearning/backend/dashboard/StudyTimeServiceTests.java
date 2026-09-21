package vn.enlearning.backend.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.dashboard.dto.StudyPeriod;
import vn.enlearning.backend.dashboard.dto.StudyTimePointResponse;
import vn.enlearning.backend.dashboard.dto.StudyTimeResponse;
import vn.enlearning.backend.dashboard.service.StudyTimeService;
import vn.enlearning.backend.entity.StudySession;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.entity.enums.StudySkill;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/**
 * Biên tuần/tháng và lệch múi giờ của biểu đồ thời gian học, trên MySQL thật (giao dịch tự rollback). Đồng hồ cố
 * định vào thứ Tư 16/09/2026 12:00 giờ Việt Nam (05:00 UTC) để kết quả không phụ thuộc ngày chạy test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudyTimeServiceTests {

	private static final Instant NOW = Instant.parse("2026-09-16T05:00:00Z");

	@Autowired
	private UserRepository users;
	@Autowired
	private UserSettingRepository settings;
	@Autowired
	private StudySessionRepository sessions;

	private StudyTimeService service;
	private User user;

	@BeforeEach
	void setUp() {
		service = new StudyTimeService(sessions, settings, Clock.fixed(NOW, ZoneOffset.UTC));
		user = new User();
		user.setEmail("it-" + UUID.randomUUID() + "@test.local");
		user.setPasswordHash("khong-dung-de-dang-nhap");
		user.setFullName("Người thử");
		users.saveAndFlush(user);
	}

	private void setZone(String zone) {
		UserSetting setting = new UserSetting();
		setting.setUser(user);
		setting.setTimeZone(zone);
		settings.saveAndFlush(setting);
	}

	private void session(String startedAtUtc, int activeSeconds) {
		StudySession s = new StudySession();
		s.setUser(user);
		s.setSkill(StudySkill.VOCABULARY);
		Instant start = Instant.parse(startedAtUtc);
		s.setStartedAt(start);
		s.setLastHeartbeatAt(start.plusSeconds(activeSeconds));
		s.setActiveSeconds(activeSeconds);
		sessions.saveAndFlush(s);
	}

	private static List<Long> minutes(StudyTimeResponse chart) {
		return chart.points().stream().map(StudyTimePointResponse::minutes).toList();
	}

	private static List<Long> previous(StudyTimeResponse chart) {
		return chart.points().stream().map(StudyTimePointResponse::previousMinutes).toList();
	}

	@Test
	@DisplayName("Người chưa học gì: đủ cột, toàn số 0, phần trăm 0, không chia cho 0")
	void emptyHistory() {
		StudyTimeResponse week = service.chart(user.getId(), StudyPeriod.WEEK);
		StudyTimeResponse month = service.chart(user.getId(), StudyPeriod.MONTH);

		assertThat(week.points()).hasSize(7);
		assertThat(month.points()).hasSize(4);
		assertThat(minutes(week)).containsOnly(0L);
		assertThat(previous(month)).containsOnly(0L);
		assertThat(week.totalMinutes()).isZero();
		assertThat(week.previousTotalMinutes()).isZero();
		assertThat(week.changePercent()).isZero();
		assertThat(month.changePercent()).isZero();
	}

	@Test
	@DisplayName("Tuần: 23:30 Chủ nhật và 00:10 thứ Hai giờ Việt Nam rơi vào hai tuần khác nhau, dù cùng ngày UTC")
	void weekBoundaryInVietnamTime() {
		// CN 13/09 23:30 giờ VN = 16:30 UTC cùng ngày -> tuần trước, cột CN.
		session("2026-09-13T16:30:00Z", 600);
		// T2 14/09 00:10 giờ VN = 17:10 UTC ngày 13 (gom theo UTC sẽ sai thành tuần trước) -> tuần này, cột T2.
		session("2026-09-13T17:10:00Z", 300);
		// CN 20/09 23:59 giờ VN -> tuần này, cột CN; T2 21/09 00:00 giờ VN -> tuần sau, không tính.
		session("2026-09-20T16:59:00Z", 120);
		session("2026-09-20T17:00:00Z", 60);

		StudyTimeResponse week = service.chart(user.getId(), StudyPeriod.WEEK);

		assertThat(minutes(week)).containsExactly(5L, 0L, 0L, 0L, 0L, 0L, 2L);
		assertThat(previous(week)).containsExactly(0L, 0L, 0L, 0L, 0L, 0L, 10L);
		assertThat(week.totalMinutes()).isEqualTo(7);
		assertThat(week.previousTotalMinutes()).isEqualTo(10);
		// (420 - 600) / 600 = -30%.
		assertThat(week.changePercent()).isEqualTo(-30);
	}

	@Test
	@DisplayName("Cùng dữ liệu nhưng múi giờ UTC thì cột dịch theo, chứng tỏ gom theo múi giờ người dùng")
	void sameDataInUtc() {
		setZone("UTC");
		session("2026-09-13T16:30:00Z", 600);
		session("2026-09-13T17:10:00Z", 300);
		session("2026-09-20T16:59:00Z", 120);
		session("2026-09-20T17:00:00Z", 60);

		StudyTimeResponse week = service.chart(user.getId(), StudyPeriod.WEEK);

		// Theo UTC: hai phiên đầu cùng là CN 13/09 (tuần trước); hai phiên sau cùng là CN 20/09 (tuần này).
		assertThat(previous(week)).containsExactly(0L, 0L, 0L, 0L, 0L, 0L, 15L);
		assertThat(minutes(week)).containsExactly(0L, 0L, 0L, 0L, 0L, 0L, 3L);
		assertThat(week.changePercent()).isEqualTo(-80);
	}

	@Test
	@DisplayName("Tháng: chia 4 nhóm 1-7, 8-14, 15-21, 22-hết; 00:30 ngày 1 giờ VN vẫn thuộc tháng mới")
	void monthBuckets() {
		// 01/09 00:30 giờ VN = 31/08 17:30 UTC -> tháng này, nhóm 1.
		session("2026-08-31T17:30:00Z", 60);
		session("2026-09-07T05:00:00Z", 60);
		session("2026-09-08T05:00:00Z", 120);
		session("2026-09-30T05:00:00Z", 180);
		// Tháng trước: 15/08 (nhóm 3), 22/08 và 31/08 giờ VN (nhóm 4).
		session("2026-08-15T05:00:00Z", 600);
		session("2026-08-22T05:00:00Z", 60);
		session("2026-08-31T05:00:00Z", 60);

		StudyTimeResponse month = service.chart(user.getId(), StudyPeriod.MONTH);

		assertThat(minutes(month)).containsExactly(2L, 2L, 0L, 3L);
		assertThat(previous(month)).containsExactly(0L, 0L, 10L, 2L);
		assertThat(month.totalMinutes()).isEqualTo(7);
		assertThat(month.previousTotalMinutes()).isEqualTo(12);
		assertThat(month.changePercent()).isEqualTo(-42);
	}

	@Test
	@DisplayName("Kỳ trước trống thì phần trăm là 0 chứ không phải vô cực")
	void previousPeriodEmpty() {
		session("2026-09-15T05:00:00Z", 1800);

		StudyTimeResponse week = service.chart(user.getId(), StudyPeriod.WEEK);

		assertThat(week.totalMinutes()).isEqualTo(30);
		assertThat(week.previousTotalMinutes()).isZero();
		assertThat(week.changePercent()).isZero();
	}

	@Test
	@DisplayName("Múi giờ hỏng trong DB thì lùi về Asia/Ho_Chi_Minh; period sai là ApiException 400")
	void fallbacksAndValidation() {
		setZone("Khong/Ton-Tai");
		assertThat(service.zoneOf(user.getId()).getId()).isEqualTo("Asia/Ho_Chi_Minh");

		assertThat(StudyPeriod.parse(null)).isEqualTo(StudyPeriod.WEEK);
		assertThat(StudyPeriod.parse("month")).isEqualTo(StudyPeriod.MONTH);
		assertThat(StudyPeriod.parse(" WEEK ")).isEqualTo(StudyPeriod.WEEK);
		assertThatThrownBy(() -> StudyPeriod.parse("year")).isInstanceOf(ApiException.class);
	}
}
