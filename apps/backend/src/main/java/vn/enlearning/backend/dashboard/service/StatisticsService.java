package vn.enlearning.backend.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository.SkillAverage;
import vn.enlearning.backend.dashboard.dto.SkillStatResponse;
import vn.enlearning.backend.dashboard.dto.StatisticsOverviewResponse;
import vn.enlearning.backend.dashboard.dto.StudyPeriod;
import vn.enlearning.backend.dashboard.dto.StudyTimeResponse;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.practice.dto.AttemptHistoryItemResponse;
import vn.enlearning.backend.practice.service.AttemptResultMapper;

/**
 * Trang Thống kê. Điểm trung bình và số lượt tính trên mọi lượt đã nộp; {@code change} là điểm trung bình 30 ngày gần
 * nhất trừ 30 ngày liền trước, và là 0 khi một trong hai khoảng không có lượt nào. Chỉ có Nghe, Đọc và Kiểm tra vì
 * Từ vựng không chấm điểm, còn Viết/Nói thêm ở đợt 4.
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

	private static final int RECENT_ATTEMPTS = 8;
	private static final Duration WINDOW = Duration.ofDays(30);

	private final StudyTimeService studyTime;
	private final PracticeAttemptRepository attempts;
	private final AttemptResultMapper mapper;
	private final Clock clock;

	@Transactional(readOnly = true)
	public StatisticsOverviewResponse overview(UUID userId) {
		ZoneId zone = studyTime.zoneOf(userId);
		StudyTimeResponse week = studyTime.chart(userId, zone, StudyPeriod.WEEK);
		StudyTimeResponse month = studyTime.chart(userId, zone, StudyPeriod.MONTH);

		Instant now = clock.instant();
		Windows windows = new Windows(now.minus(WINDOW.multipliedBy(2)), now.minus(WINDOW), now.plus(Duration.ofDays(1)));
		List<SkillStatResponse> skills = List.of(
				skill(Skill.LISTENING, (from, to) -> attempts.listeningAverage(userId, from, to), windows),
				skill(Skill.READING, (from, to) -> attempts.readingAverage(userId, from, to), windows),
				skill(Skill.EXAM, (from, to) -> attempts.examAverage(userId, from, to), windows));

		List<AttemptHistoryItemResponse> recent = attempts
				.findByUserIdAndStatus(userId, AttemptStatus.COMPLETED, PageRequest.of(0, RECENT_ATTEMPTS,
						Sort.by(Sort.Order.desc("submittedAt"), Sort.Order.desc("id"))))
				.stream().filter(StatisticsService::hasParent).map(mapper::toHistoryItem).toList();

		return new StatisticsOverviewResponse(week.totalMinutes(), week.previousTotalMinutes(), week.points(),
				month.points(), skills, recent);
	}

	/** {@code previousStart} đến {@code recentStart} là 30 ngày trước đó, {@code recentStart} đến {@code end} là 30 ngày gần nhất. */
	private record Windows(Instant previousStart, Instant recentStart, Instant end) {
	}

	private interface Aggregate {
		SkillAverage between(Instant from, Instant to);
	}

	private static SkillStatResponse skill(Skill skill, Aggregate query, Windows w) {
		SkillAverage all = query.between(Instant.EPOCH, w.end());
		SkillAverage recent = query.between(w.recentStart(), w.end());
		SkillAverage before = query.between(w.previousStart(), w.recentStart());
		double change = recent.getAttempts() == 0 || before.getAttempts() == 0 ? 0
				: round(recent.getAverage() - before.getAverage());
		return new SkillStatResponse(skill, all.getAverage() == null ? 0 : round(all.getAverage()),
				all.getAttempts(), change);
	}

	private static double round(double value) {
		return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
	}

	/** Nội dung đã bị xoá mềm làm cha rỗng; bỏ qua để không lỗi cả trang. */
	private static boolean hasParent(PracticeAttempt attempt) {
		return attempt.getListeningLesson() != null || attempt.getReadingLesson() != null
				|| attempt.getExam() != null;
	}
}
