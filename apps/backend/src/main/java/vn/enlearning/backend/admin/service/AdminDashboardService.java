package vn.enlearning.backend.admin.service;

import java.time.Clock;
import java.time.Instant;
import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminDashboardResponse;
import vn.enlearning.backend.admin.dto.AdminDashboardResponse.ContentCount;
import vn.enlearning.backend.admin.dto.AdminDashboardResponse.Overview;
import vn.enlearning.backend.admin.dto.AdminDashboardResponse.SystemStats;
import vn.enlearning.backend.admin.dto.AdminDashboardResponse.SignupPoint;
import vn.enlearning.backend.admin.dto.ContentType;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.EmailStatus;
import vn.enlearning.backend.entity.enums.Role;
import vn.enlearning.backend.mail.EmailLogRepository;
import vn.enlearning.backend.study.repository.StudySessionRepository;

/** Số liệu tổng quan cho Admin. Tháng đăng ký tính theo giờ Việt Nam, sáu tháng gần nhất; số theo thời gian lấy 30 ngày gần nhất. */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

	private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
	private static final int SIGNUP_MONTHS = 6;
	private static final Duration WINDOW = Duration.ofDays(30);

	private final UserRepository users;
	private final StudySessionRepository studySessions;
	private final FlashcardDeckRepository decks;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final WritingPromptRepository writingPrompts;
	private final SpeakingLessonRepository speakingLessons;
	private final ExamRepository exams;
	private final EmailLogRepository emailLogs;
	private final Clock clock;

	@Transactional(readOnly = true)
	public AdminDashboardResponse get() {
		Instant now = clock.instant();
		Instant since = now.minus(WINDOW);

		List<ContentCount> contentCounts = List.of(
				new ContentCount(ContentType.VOCABULARY, decks.count()),
				new ContentCount(ContentType.LISTENING, listeningLessons.count()),
				new ContentCount(ContentType.READING, readingLessons.count()),
				new ContentCount(ContentType.WRITING, writingPrompts.count()),
				new ContentCount(ContentType.SPEAKING, speakingLessons.count()),
				new ContentCount(ContentType.EXAM, exams.count()));

		Overview overview = new Overview(users.count(), users.countByCreatedAtGreaterThanEqual(since),
				users.countByStatusAndLastActiveAtGreaterThanEqual(AccountStatus.ACTIVE, since),
				studySessions.countByStartedAtGreaterThanEqual(since),
				contentCounts.stream().mapToLong(ContentCount::count).sum());
		SystemStats system = new SystemStats(users.countByRole(Role.ADMIN), users.countByStatus(AccountStatus.LOCKED),
				users.countByStatus(AccountStatus.PENDING),
				emailLogs.countByStatusAndCreatedAtGreaterThanEqual(EmailStatus.SENT, since),
				emailLogs.countByStatusAndCreatedAtGreaterThanEqual(EmailStatus.FAILED, since));
		return new AdminDashboardResponse(overview, signups(now), contentCounts, system, List.of());
	}

	private List<SignupPoint> signups(Instant now) {
		YearMonth current = YearMonth.from(now.atZone(ZONE));
		YearMonth first = current.minusMonths(SIGNUP_MONTHS - 1L);
		Map<YearMonth, Long> perMonth = users.createdAtSince(first.atDay(1).atStartOfDay(ZONE).toInstant()).stream()
				.collect(Collectors.groupingBy(at -> YearMonth.from(at.atZone(ZONE)), Collectors.counting()));
		List<SignupPoint> points = new ArrayList<>();
		for (YearMonth month = first; !month.isAfter(current); month = month.plusMonths(1)) {
			String vi = "T" + month.getMonthValue();
			String en = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
			points.add(new SignupPoint(new L10n(vi, en), perMonth.getOrDefault(month, 0L)));
		}
		return points;
	}
}
