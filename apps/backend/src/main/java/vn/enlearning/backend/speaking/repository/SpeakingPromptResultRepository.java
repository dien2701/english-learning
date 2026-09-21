package vn.enlearning.backend.speaking.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.SpeakingPromptResult;

public interface SpeakingPromptResultRepository extends JpaRepository<SpeakingPromptResult, UUID> {

	Optional<SpeakingPromptResult> findByAttemptIdAndPromptId(UUID attemptId, UUID promptId);

	List<SpeakingPromptResult> findByAttemptIdOrderByCreatedAtAscIdAsc(UUID attemptId);
}
