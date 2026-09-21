package vn.enlearning.backend.content.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.enlearning.backend.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
}
