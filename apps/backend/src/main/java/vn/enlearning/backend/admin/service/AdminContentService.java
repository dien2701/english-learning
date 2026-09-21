package vn.enlearning.backend.admin.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.admin.dto.AdminContentDetailResponse;
import vn.enlearning.backend.admin.dto.AdminContentRequest;
import vn.enlearning.backend.admin.dto.AdminContentSummaryResponse;
import vn.enlearning.backend.admin.dto.ContentStatusResponse;
import vn.enlearning.backend.admin.dto.ContentType;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.common.PageResponse;
import vn.enlearning.backend.config.CacheConfig;
import vn.enlearning.backend.content.repository.ExamRepository;
import vn.enlearning.backend.content.repository.FlashcardDeckRepository;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.content.repository.ReadingLessonRepository;
import vn.enlearning.backend.content.repository.SpeakingLessonRepository;
import vn.enlearning.backend.content.repository.WritingPromptRepository;
import vn.enlearning.backend.entity.ContentEntity;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;

/**
 * CRUD nội dung cho Admin. Nội dung đã có trong lịch sử học (xem {@link ContentUsage}) không bị xoá và không bị bớt
 * phần tử con: trả 409 {@code CONTENT_IN_USE}, Admin chỉ chuyển được sang INACTIVE. Người học chỉ thấy nội dung ACTIVE
 * nên đổi trạng thái có hiệu lực ngay, không cần thêm bước nào.
 */
@Service
@RequiredArgsConstructor
public class AdminContentService {

	private final FlashcardDeckRepository decks;
	private final ListeningLessonRepository listeningLessons;
	private final ReadingLessonRepository readingLessons;
	private final WritingPromptRepository writingPrompts;
	private final SpeakingLessonRepository speakingLessons;
	private final ExamRepository exams;
	private final AdminContentWriter writer;
	private final AdminContentReader reader;
	private final ContentUsage usage;

	@Transactional(readOnly = true)
	public PageResponse<AdminContentSummaryResponse> list(String search, String skill, String status, String level,
			int page, int pageSize) {
		ContentType typeFilter = parse(ContentType.class, skill);
		ContentStatus statusFilter = parse(ContentStatus.class, status);
		Level levelFilter = parse(Level.class, level);

		List<ContentEntity> found = new ArrayList<>();
		for (ContentType type : ContentType.values()) {
			if (typeFilter != null && typeFilter != type) {
				continue;
			}
			found.addAll(switch (type) {
				case VOCABULARY -> decks.findAll(AdminContentSpecs.filter(search, statusFilter, levelFilter, true));
				case LISTENING -> listeningLessons.findAll(AdminContentSpecs.filter(search, statusFilter, levelFilter, true));
				case READING -> readingLessons.findAll(AdminContentSpecs.filter(search, statusFilter, levelFilter, true));
				case WRITING -> writingPrompts.findAll(AdminContentSpecs.filter(search, statusFilter, levelFilter, true));
				case SPEAKING -> speakingLessons.findAll(AdminContentSpecs.filter(search, statusFilter, levelFilter, true));
				case EXAM -> exams.findAll(AdminContentSpecs.filter(search, statusFilter, levelFilter, false));
			});
		}
		found.sort(Comparator.comparing(ContentEntity::getUpdatedAt, Comparator.nullsLast(Comparator.<Instant>reverseOrder()))
				.thenComparing(ContentEntity::getId));
		return PageResponse.of(reader.summaries(found), page, pageSize);
	}

	@Transactional(readOnly = true)
	public AdminContentDetailResponse get(UUID id) {
		return reader.detail(find(id));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.TOPICS, allEntries = true)
	public AdminContentDetailResponse create(UUID adminId, AdminContentRequest request) {
		UUID id = writer.create(adminId, request);
		return reader.detail(find(id));
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.TOPICS, allEntries = true)
	public AdminContentDetailResponse update(UUID id, AdminContentRequest request) {
		ContentEntity entity = find(id);
		writer.update(entity, request, usage.isUsed(AdminContentReader.typeOf(entity), id));
		flushOrInUse(entity);
		return reader.detail(entity);
	}

	@Transactional
	@CacheEvict(cacheNames = CacheConfig.TOPICS, allEntries = true)
	public ContentStatusResponse setStatus(UUID id, ContentStatus status) {
		ContentEntity entity = find(id);
		entity.setStatus(status);
		return new ContentStatusResponse(id, status);
	}

	/** Xoá mềm nội dung chưa từng được học; đã có trong lịch sử thì 409. */
	@Transactional
	@CacheEvict(cacheNames = CacheConfig.TOPICS, allEntries = true)
	public void delete(UUID id) {
		ContentEntity entity = find(id);
		ContentType type = AdminContentReader.typeOf(entity);
		if (usage.isUsed(type, id)) {
			throw new ApiException(ErrorCode.CONTENT_IN_USE);
		}
		repository(type).delete(entity);
		flushOrInUse(entity);
	}

	// --- nội bộ -----------------------------------------------------------------------------------

	private ContentEntity find(UUID id) {
		for (ContentType type : ContentType.values()) {
			Optional<? extends ContentEntity> found = repository(type).findById(id);
			if (found.isPresent()) {
				return found.get();
			}
		}
		throw new ApiException(ErrorCode.NOT_FOUND);
	}

	@SuppressWarnings("unchecked")
	private JpaRepository<ContentEntity, UUID> repository(ContentType type) {
		JpaRepository<?, UUID> repo = switch (type) {
			case VOCABULARY -> decks;
			case LISTENING -> listeningLessons;
			case READING -> readingLessons;
			case WRITING -> writingPrompts;
			case SPEAKING -> speakingLessons;
			case EXAM -> exams;
		};
		return (JpaRepository<ContentEntity, UUID>) repo;
	}

	/** Đẩy thay đổi xuống DB ngay để lỗi khoá ngoại thành 409 có chủ đích thay vì 500 lúc commit. */
	private void flushOrInUse(ContentEntity entity) {
		try {
			repository(AdminContentReader.typeOf(entity)).flush();
		} catch (DataIntegrityViolationException ex) {
			throw new ApiException(ErrorCode.CONTENT_IN_USE);
		}
	}

	private static <E extends Enum<E>> E parse(Class<E> type, String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Enum.valueOf(type, value.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw ApiException.field(ErrorCode.VALIDATION, type.getSimpleName().toLowerCase(), "errors.badRequest");
		}
	}
}
