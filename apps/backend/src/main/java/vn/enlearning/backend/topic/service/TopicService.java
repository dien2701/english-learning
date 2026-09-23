package vn.enlearning.backend.topic.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.common.L10n;
import vn.enlearning.backend.config.CacheConfig;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.TopicRepository;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.topic.dto.TopicResponse;

@Service
@RequiredArgsConstructor
public class TopicService {

	private final TopicRepository topics;
	private final FlashcardDeckRepository decks;

	@Transactional(readOnly = true)
	@Cacheable(CacheConfig.TOPICS)
	public List<TopicResponse> list() {
		return topics.findAll().stream()
				.sorted(Comparator.comparing(t -> t.getNameVi().toLowerCase()))
				.map(t -> new TopicResponse(t.getId(), L10n.of(t.getNameVi(), t.getNameEn()),
						decks.countByTopicIdAndStatus(t.getId(), ContentStatus.ACTIVE), t.getImageUrl(),
						t.getImageAuthor(), t.getImageAuthorUrl()))
				.toList();
	}
}
