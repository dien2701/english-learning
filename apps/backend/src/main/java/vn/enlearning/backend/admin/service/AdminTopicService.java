package vn.enlearning.backend.admin.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminTopicResponse;
import vn.enlearning.backend.admin.dto.TopicRequest;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.Topic;

/**
 * Chủ đề dùng chung mọi kỹ năng. Slug sinh một lần khi tạo và giữ nguyên khi đổi tên. Chủ đề còn nội dung
 * (kể cả INACTIVE) không được xoá vì khoá ngoại {@code ON DELETE RESTRICT}: trả 409 thay vì để DB báo lỗi.
 */
@Service
@RequiredArgsConstructor
public class AdminTopicService {

	private static final int SLUG_MAX = 100;

	private final TopicRepository topics;
	private final FlashcardDeckRepository decks;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final WritingPromptRepository writingPrompts;
	private final SpeakingLessonRepository speakingLessons;

	@Transactional(readOnly = true)
	public List<AdminTopicResponse> list() {
		return topics.findAll(Sort.by("nameVi", "id")).stream().map(this::toResponse).toList();
	}

	@Transactional
	public AdminTopicResponse create(TopicRequest request) {
		Topic topic = new Topic();
		topic.setSlug(uniqueSlug(request.nameEn() != null && !request.nameEn().isBlank() ? request.nameEn() : request.nameVi()));
		apply(topic, request);
		return toResponse(topics.saveAndFlush(topic));
	}

	@Transactional
	public AdminTopicResponse update(UUID id, TopicRequest request) {
		Topic topic = find(id);
		apply(topic, request);
		return toResponse(topics.saveAndFlush(topic));
	}

	@Transactional
	public void delete(UUID id) {
		Topic topic = find(id);
		if (itemCount(id) > 0) {
			throw new ApiException(ErrorCode.CONTENT_IN_USE);
		}
		topics.delete(topic);
		topics.flush();
	}

	private Topic find(UUID id) {
		return topics.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	private static void apply(Topic topic, TopicRequest request) {
		topic.setNameVi(request.nameVi().trim());
		topic.setNameEn(request.nameEn() == null || request.nameEn().isBlank() ? null : request.nameEn().trim());
	}

	private long itemCount(UUID topicId) {
		return decks.countByTopicId(topicId) + listeningLessons.countByTopicId(topicId)
				+ readingLessons.countByTopicId(topicId) + writingPrompts.countByTopicId(topicId)
				+ speakingLessons.countByTopicId(topicId);
	}

	private AdminTopicResponse toResponse(Topic topic) {
		return new AdminTopicResponse(topic.getId(), topic.getSlug(), L10n.of(topic.getNameVi(), topic.getNameEn()),
				itemCount(topic.getId()));
	}

	private String uniqueSlug(String name) {
		String base = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}+", "")
				.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
		if (base.isEmpty()) {
			base = "topic";
		}
		base = base.substring(0, Math.min(base.length(), SLUG_MAX - 6));
		String slug = base;
		for (int suffix = 2; topics.countBySlugIncludingDeleted(slug) > 0; suffix++) {
			slug = base + "-" + suffix;
		}
		return slug;
	}
}
