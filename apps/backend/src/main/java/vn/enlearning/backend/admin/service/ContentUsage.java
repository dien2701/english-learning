package vn.enlearning.backend.admin.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.ContentType;
import vn.enlearning.backend.content.repository.PracticeAttemptRepository;
import vn.enlearning.backend.flashcard.repository.UserFlashcardProgressRepository;
import vn.enlearning.backend.speaking.repository.SpeakingAttemptRepository;
import vn.enlearning.backend.writing.repository.WritingSubmissionRepository;

/** Nội dung nào đã nằm trong lịch sử học của người dùng: những nội dung này không được xoá hay bớt phần tử con. */
@Component
@RequiredArgsConstructor
class ContentUsage {

	private final UserFlashcardProgressRepository flashcardProgress;
	private final PracticeAttemptRepository attempts;
	private final WritingSubmissionRepository submissions;
	private final SpeakingAttemptRepository speakingAttempts;

	Set<UUID> used(ContentType type, Collection<UUID> ids) {
		if (ids.isEmpty()) {
			return Set.of();
		}
		List<UUID> found = switch (type) {
			case VOCABULARY -> flashcardProgress.usedDeckIds(ids);
			case LISTENING -> attempts.usedListeningIds(ids);
			case READING -> attempts.usedReadingIds(ids);
			case EXAM -> attempts.usedExamIds(ids);
			case WRITING -> submissions.usedPromptIds(ids);
			case SPEAKING -> speakingAttempts.usedLessonIds(ids);
		};
		return new HashSet<>(found);
	}

	boolean isUsed(ContentType type, UUID id) {
		return !used(type, List.of(id)).isEmpty();
	}
}
