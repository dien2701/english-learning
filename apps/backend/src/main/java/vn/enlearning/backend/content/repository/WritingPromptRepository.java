package vn.enlearning.backend.content.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.WritingPrompt;

public interface WritingPromptRepository extends JpaRepository<WritingPrompt, UUID> {
}
