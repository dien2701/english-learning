package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, UUID> {

	Optional<Topic> findBySlug(String slug);
}
