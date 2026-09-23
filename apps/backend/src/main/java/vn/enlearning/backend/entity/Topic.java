package vn.enlearning.backend.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/** Chủ đề nội dung, dùng chung cho mọi kỹ năng. */
@Getter
@Setter
@Entity
@Table(name = "topics",
		uniqueConstraints = @UniqueConstraint(name = "uq_topics_slug", columnNames = "slug"),
		indexes = {
				@Index(name = "idx_topics_name_vi", columnList = "name_vi"),
				@Index(name = "idx_topics_name_en", columnList = "name_en")
		})
@SQLDelete(sql = "UPDATE topics SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Topic extends SoftDeletableEntity {

	@Column(nullable = false, length = 100)
	private String slug;

	@Column(nullable = false, length = 100)
	private String nameVi;

	@Column(length = 100)
	private String nameEn;

	@Column(length = 500)
	private String imageUrl;

	@Column(length = 500)
	private String imageAuthor;

	@Column(length = 500)
	private String imageAuthorUrl;
}
