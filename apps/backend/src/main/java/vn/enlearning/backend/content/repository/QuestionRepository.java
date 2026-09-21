package vn.enlearning.backend.content.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.enlearning.backend.entity.Question;
import vn.enlearning.backend.entity.enums.Skill;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

	@EntityGraph(attributePaths = "options")
	List<Question> findByListeningLessonIdOrderBySortOrder(UUID lessonId);

	@EntityGraph(attributePaths = "options")
	List<Question> findByReadingLessonIdOrderBySortOrder(UUID lessonId);

	@EntityGraph(attributePaths = "options")
	List<Question> findByExamIdOrderBySortOrder(UUID examId);

	interface ParentCount {
		UUID getParentId();

		long getTotal();
	}

	interface ExamSkillCount {
		UUID getParentId();

		Skill getSkill();

		long getTotal();
	}

	@Query("select q.listeningLesson.id as parentId, count(q) as total from Question q "
			+ "where q.listeningLesson.id in :ids group by q.listeningLesson.id")
	List<ParentCount> countByListeningLessons(@Param("ids") Collection<UUID> ids);

	@Query("select q.readingLesson.id as parentId, count(q) as total from Question q "
			+ "where q.readingLesson.id in :ids group by q.readingLesson.id")
	List<ParentCount> countByReadingLessons(@Param("ids") Collection<UUID> ids);

	@Query("select q.exam.id as parentId, q.skill as skill, count(q) as total from Question q "
			+ "where q.exam.id in :ids group by q.exam.id, q.skill")
	List<ExamSkillCount> countByExams(@Param("ids") Collection<UUID> ids);
}
