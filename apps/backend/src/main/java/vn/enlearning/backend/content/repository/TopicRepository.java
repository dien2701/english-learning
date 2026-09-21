package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

	Optional<Topic> findBySlug(String slug);

	/** Native để tính cả chủ đề đã xoá mềm: slug của chúng vẫn nằm trong unique index. */
	@Query(value = "SELECT COUNT(*) FROM topics WHERE slug = :slug", nativeQuery = true)
	long countBySlugIncludingDeleted(@Param("slug") String slug);
}
