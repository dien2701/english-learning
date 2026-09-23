package vn.enlearning.backend.content.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.enlearning.backend.entity.Exam;
import vn.enlearning.backend.entity.enums.ContentStatus;

public interface ExamRepository extends JpaRepository<Exam, UUID>, JpaSpecificationExecutor<Exam> {

	Optional<Exam> findByIdAndStatus(UUID id, ContentStatus status);

	/** Khoá idempotent của seeder dữ liệu thật (đợt 13.5): bỏ qua đề đã có cùng tiêu đề. */
	boolean existsByTitleVi(String titleVi);
}
