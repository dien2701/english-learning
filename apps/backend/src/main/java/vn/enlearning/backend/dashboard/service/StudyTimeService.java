package vn.enlearning.backend.dashboard.service;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.repository.UserSettingRepository;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.dashboard.dto.StudyPeriod;
import vn.enlearning.backend.dashboard.dto.StudyTimePointResponse;
import vn.enlearning.backend.dashboard.dto.StudyTimeResponse;
import vn.enlearning.backend.entity.UserSetting;
import vn.enlearning.backend.study.repository.StudySessionRepository;
import vn.enlearning.backend.study.repository.StudySessionRepository.SessionSlice;

/**
 * Biểu đồ thời gian học. Phiên học tính vào ngày (theo múi giờ của người dùng) của {@code startedAt}. Tuần chạy từ
 * thứ Hai đến Chủ nhật (7 cột); tháng là tháng dương lịch chia 4 nhóm ngày 1-7, 8-14, 15-21, 22-hết. Kỳ trước dùng
 * cùng cách chia nên cột nào cũng có cột đối chiếu; các cột chưa tới (tương lai) là 0.
 */
@Service
@RequiredArgsConstructor
public class StudyTimeService {

	private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final int MONTH_BUCKETS = 4;
	private static final int DAYS_PER_BUCKET = 7;

	private static final List<L10n> WEEK_LABELS = List.of(new L10n("T2", "Mon"), new L10n("T3", "Tue"),
			new L10n("T4", "Wed"), new L10n("T5", "Thu"), new L10n("T6", "Fri"), new L10n("T7", "Sat"),
			new L10n("CN", "Sun"));
	private static final List<L10n> MONTH_LABELS = List.of(new L10n("Tuần 1", "Week 1"), new L10n("Tuần 2", "Week 2"),
			new L10n("Tuần 3", "Week 3"), new L10n("Tuần 4", "Week 4"));

	private final StudySessionRepository sessions;
	private final UserSettingRepository settings;
	private final Clock clock;

	/** Múi giờ trong cài đặt; thiếu hoặc không hợp lệ thì lùi về Asia/Ho_Chi_Minh. */
	@Transactional(readOnly = true)
	public ZoneId zoneOf(UUID userId) {
		return settings.findByUserId(userId).map(UserSetting::getTimeZone).map(StudyTimeService::parseZone)
				.orElse(DEFAULT_ZONE);
	}

	@Transactional(readOnly = true)
	public StudyTimeResponse chart(UUID userId, StudyPeriod period) {
		return chart(userId, zoneOf(userId), period);
	}

	@Transactional(readOnly = true)
	public StudyTimeResponse chart(UUID userId, ZoneId zone, StudyPeriod period) {
		LocalDate today = LocalDate.now(clock.withZone(zone));
		boolean week = period == StudyPeriod.WEEK;
		LocalDate start = week ? today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
				: today.withDayOfMonth(1);
		LocalDate end = week ? start.plusWeeks(1) : start.plusMonths(1);
		LocalDate previousStart = week ? start.minusWeeks(1) : start.minusMonths(1);

		Instant from = previousStart.atStartOfDay(zone).toInstant();
		Instant to = end.atStartOfDay(zone).toInstant();
		int buckets = week ? WEEK_LABELS.size() : MONTH_BUCKETS;
		long[] current = new long[buckets];
		long[] previous = new long[buckets];

		for (SessionSlice slice : sessions.findSlices(userId, from, to)) {
			LocalDate day = slice.getStartedAt().atZone(zone).toLocalDate();
			if (!day.isBefore(start) && day.isBefore(end)) {
				current[bucket(week, start, day)] += slice.getActiveSeconds();
			} else if (!day.isBefore(previousStart) && day.isBefore(start)) {
				previous[bucket(week, previousStart, day)] += slice.getActiveSeconds();
			}
		}

		List<L10n> labels = week ? WEEK_LABELS : MONTH_LABELS;
		List<StudyTimePointResponse> points = new ArrayList<>(buckets);
		long currentSeconds = 0;
		long previousSeconds = 0;
		for (int i = 0; i < buckets; i++) {
			points.add(new StudyTimePointResponse(labels.get(i), minutes(current[i]), minutes(previous[i])));
			currentSeconds += current[i];
			previousSeconds += previous[i];
		}
		return new StudyTimeResponse(period, points, minutes(currentSeconds), minutes(previousSeconds),
				changePercent(currentSeconds, previousSeconds));
	}

	private static int bucket(boolean week, LocalDate periodStart, LocalDate day) {
		if (week) {
			return (int) ChronoUnit.DAYS.between(periodStart, day);
		}
		// Ngày 29-31 dồn vào nhóm cuối.
		return Math.min(MONTH_BUCKETS - 1, (day.getDayOfMonth() - 1) / DAYS_PER_BUCKET);
	}

	/** Làm tròn từ giây; tổng làm tròn riêng nên có thể lệch 1 phút so với tổng các cột. */
	static long minutes(long seconds) {
		return Math.round(seconds / 60.0);
	}

	/** Kỳ trước trống (chia cho 0) trả 0, không phải vô cực hay NaN. */
	static int changePercent(long currentSeconds, long previousSeconds) {
		if (previousSeconds <= 0) {
			return 0;
		}
		return (int) Math.round((currentSeconds - previousSeconds) * 100.0 / previousSeconds);
	}

	private static ZoneId parseZone(String id) {
		try {
			return ZoneId.of(id);
		} catch (DateTimeException e) {
			return DEFAULT_ZONE;
		}
	}
}
