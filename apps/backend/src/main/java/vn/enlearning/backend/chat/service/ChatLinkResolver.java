package vn.enlearning.backend.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.ChatSuggestionLink;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.StudySkill;

/**
 * Đổi nhóm bài mà trợ lý gợi ý thành liên kết tới một nội dung ACTIVE có thật (đường dẫn khớp route của
 * frontend), nên liên kết không bao giờ trỏ vào trang không tồn tại. Nhóm không có nội dung thì bỏ qua.
 */
@Component
@RequiredArgsConstructor
class ChatLinkResolver {

	static final int MAX_LINKS = 2;

	private final ListeningLessonRepository listening;
	private final ReadingLessonRepository reading;
	private final WritingPromptRepository writing;
	private final SpeakingLessonRepository speaking;
	private final FlashcardDeckRepository decks;

	List<ChatSuggestionLink> resolve(List<StudySkill> skills) {
		List<ChatSuggestionLink> links = new ArrayList<>();
		for (StudySkill skill : skills.stream().distinct().toList()) {
			if (links.size() >= MAX_LINKS) {
				break;
			}
			link(skill).ifPresent(links::add);
		}
		return links;
	}

	private Optional<ChatSuggestionLink> link(StudySkill skill) {
		ContentStatus active = ContentStatus.ACTIVE;
		return switch (skill) {
			case LISTENING -> listening.findFirstByStatusOrderByCreatedAtAscIdAsc(active)
					.map(c -> of(c, skill, "/listening/"));
			case READING -> reading.findFirstByStatusOrderByCreatedAtAscIdAsc(active)
					.map(c -> of(c, skill, "/reading/"));
			case WRITING -> writing.findFirstByStatusOrderByCreatedAtAscIdAsc(active)
					.map(c -> of(c, skill, "/writing/"));
			case SPEAKING -> speaking.findFirstByStatusOrderByCreatedAtAscIdAsc(active)
					.map(c -> of(c, skill, "/speaking/"));
			case VOCABULARY -> decks.findFirstByStatusOrderByCreatedAtAscIdAsc(active)
					.map(c -> of(c, skill, "/flashcard/"));
			case EXAM, CHAT -> Optional.empty();
		};
	}

	private static ChatSuggestionLink of(ContentEntity content, StudySkill skill, String prefix) {
		return new ChatSuggestionLink(content.getTitleVi(), prefix + content.getId(), skill);
	}
}
