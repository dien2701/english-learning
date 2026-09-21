package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.enlearning.backend.entity.ReadingLesson;
import vn.enlearning.backend.entity.enums.ContentStatus;

public interface ReadingLessonRepository
		extends JpaRepository<ReadingLesson, UUID>, JpaSpecificationExecutor<ReadingLesson> {

	Optional<ReadingLesson> findByIdAndStatus(UUID id, ContentStatus status);
}
