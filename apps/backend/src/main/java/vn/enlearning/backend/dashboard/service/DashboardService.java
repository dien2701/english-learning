package vn.enlearning.backend.dashboard.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.auth.repository.UserRepository;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.FlashcardRepository;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.dashboard.dto.AttendedLessonResponse;
import vn.enlearning.backend.dashboard.dto.AttendedLessonResponse.LessonStatus;
import vn.enlearning.backend.dashboard.dto.ContinueLearningResponse;
import vn.enlearning.backend.dashboard.dto.DashboardSummaryResponse;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.FlashcardDeck;
import vn.enlearning.backend.entity.PracticeAttempt;
import vn.enlearning.backend.entity.User;
import vn.enlearning.backend.entity.enums.AttemptStatus;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.RecallLevel;
import vn.enlearning.backend.entity.enums.Skill;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository.DeckActivity;

/**
 * Trang Dashboard: bài đang học dở và danh sách bài đã tham gia. Bộ thẻ có ít nhất một thẻ đã đánh giá là "đã tham
 * gia" (hoàn thành khi thuộc hết); Nghe, Đọc, Kiểm tra tính theo lượt nộp mới nhất của từng bài (luôn hoàn thành vì
 * lượt chỉ được lưu khi nộp). Nội dung không còn ACTIVE bị ẩn để không dẫn tới trang 404.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

	static final int MAX_LESSONS = 20;
	private static final int ATTEMPT_WINDOW = 100;

	private final UserRepository users;
	private final FlashcardDeckRepository decks;
	private final FlashcardRepository cards;
	private final UserFlashcardProgressRepository progress;
	private final PracticeAttemptRepository attempts;

	/** Một bài đã tham gia; {@code resume} chỉ có khi bài đang dở và là bộ thẻ. */
	private record Entry(AttendedLessonResponse lesson, ContinueLearningResponse resume) {
	}

	/** {@code status} là IN_PROGRESS hoặc COMPLETED để lọc; trống hoặc ALL là không lọc. */
	@Transactional(readOnly = true)
	public DashboardSummaryResponse summary(UUID userId, String status) {
		LessonStatus filter = parseStatus(status);
		User user = users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

		List<Entry> entries = new ArrayList<>(deckEntries(userId));
		entries.addAll(practiceEntries(userId));
		entries.sort(Comparator.comparing((Entry e) -> e.lesson().lastActivityAt()).reversed());

		ContinueLearningResponse resume = entries.stream().filter(e -> e.resume() != null).map(Entry::resume)
				.findFirst().orElse(null);
		List<AttendedLessonResponse> lessons = entries.stream().map(Entry::lesson)
				.filter(l -> filter == null || l.status() == filter).limit(MAX_LESSONS).toList();
		return new DashboardSummaryResponse(greetingName(user.getFullName()), resume, lessons);
	}

	// --- Bộ thẻ -----------------------------------------------------------------------------------

	private List<Entry> deckEntries(UUID userId) {
		List<DeckActivity> activity = progress.deckActivity(userId, RecallLevel.REMEMBERED);
		if (activity.isEmpty()) {
			return List.of();
		}
		Set<UUID> ids = activity.stream().map(DeckActivity::getDeckId).collect(Collectors.toSet());
		Map<UUID, FlashcardDeck> found = decks.findAllById(ids).stream()
				.filter(d -> d.getStatus() == ContentStatus.ACTIVE)
				.collect(Collectors.toMap(FlashcardDeck::getId, Function.identity()));
		Map<UUID, Long> totals = cards.countByDeckIds(found.keySet()).stream()
				.collect(Collectors.toMap(FlashcardRepository.DeckCount::getDeckId,
						FlashcardRepository.DeckCount::getTotal));

		List<Entry> entries = new ArrayList<>();
		for (DeckActivity a : activity) {
			FlashcardDeck deck = found.get(a.getDeckId());
			if (deck == null) {
				continue;
			}
			long total = totals.getOrDefault(deck.getId(), 0L);
			int percent = total == 0 ? 0 : (int) Math.round(a.getRemembered() * 100.0 / total);
			boolean done = total > 0 && a.getRemembered() >= total;
			L10n title = L10n.of(deck.getTitleVi(), deck.getTitleEn());
			AttendedLessonResponse lesson = new AttendedLessonResponse(deck.getId(), Skill.VOCABULARY, title,
					deck.getLevel(), done ? LessonStatus.COMPLETED : LessonStatus.IN_PROGRESS, percent, null,
					a.getLastReviewedAt(), "/flashcard/" + deck.getId());
			ContinueLearningResponse resume = done ? null
					: new ContinueLearningResponse(deck.getId(), Skill.VOCABULARY, title,
							L10n.of(deck.getTopic().getNameVi(), deck.getTopic().getNameEn()), deck.getLevel(),
							percent, a.getRemembered(), total,
							deck.getCoverImageUrl() == null ? "" : deck.getCoverImageUrl(),
							"/flashcard/" + deck.getId() + "/study", a.getLastReviewedAt());
			entries.add(new Entry(lesson, resume));
		}
		return entries;
	}

	// --- Nghe, Đọc, Kiểm tra -------------------------------------------------------------------------

	private List<Entry> practiceEntries(UUID userId) {
		List<PracticeAttempt> latest = attempts.findByUserIdAndStatus(userId, AttemptStatus.COMPLETED,
				PageRequest.of(0, ATTEMPT_WINDOW, Sort.by(Sort.Order.desc("submittedAt"), Sort.Order.desc("id"))))
				.getContent();

		Set<UUID> seen = new HashSet<>();
		List<Entry> entries = new ArrayList<>();
		for (PracticeAttempt attempt : latest) {
			Skill skill = attempt.getListeningLesson() != null ? Skill.LISTENING
					: attempt.getReadingLesson() != null ? Skill.READING : attempt.getExam() != null ? Skill.EXAM : null;
			ContentEntity parent = skill == null ? null : parentOf(attempt);
			// Lượt được sắp mới nhất trước nên lượt đầu tiên của mỗi bài là lượt hiển thị.
			if (parent == null || parent.getStatus() != ContentStatus.ACTIVE || !seen.add(parent.getId())) {
				continue;
			}
			String base = switch (skill) {
				case LISTENING -> "/listening/result/";
				case READING -> "/reading/result/";
				default -> "/exam/result/";
			};
			entries.add(new Entry(new AttendedLessonResponse(parent.getId(), skill,
					L10n.of(parent.getTitleVi(), parent.getTitleEn()), parent.getLevel(), LessonStatus.COMPLETED, 100,
					attempt.getScore(), attempt.getSubmittedAt(), base + attempt.getId()), null));
		}
		return entries;
	}

	private static ContentEntity parentOf(PracticeAttempt attempt) {
		if (attempt.getListeningLesson() != null) {
			return attempt.getListeningLesson();
		}
		return attempt.getReadingLesson() != null ? attempt.getReadingLesson() : attempt.getExam();
	}

	// --- nội bộ -----------------------------------------------------------------------------------

	private static LessonStatus parseStatus(String status) {
		if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim())) {
			return null;
		}
		try {
			return LessonStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw ApiException.field(ErrorCode.VALIDATION, "status", "errors.badRequest");
		}
	}

	/** Chữ cuối của họ tên ("Trịnh Xuân Diện" thành "Diện"), giống cách mock đặt lời chào. */
	static String greetingName(String fullName) {
		String[] parts = fullName == null ? new String[0] : fullName.trim().split("\\s+");
		return parts.length == 0 || parts[parts.length - 1].isEmpty() ? "" : parts[parts.length - 1];
	}
}
