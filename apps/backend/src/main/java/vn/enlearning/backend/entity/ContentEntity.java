package vn.enlearning.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.ContentStatus;
import vn.enlearning.backend.entity.enums.Level;

/**
 * Phần chung của nội dung do Admin quản lý: tiêu đề song ngữ, trình độ,
 * trạng thái và người tạo. Nội dung đã nằm trong lịch sử học không bị xoá
 * mà chuyển {@link ContentStatus#INACTIVE}.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class ContentEntity extends SoftDeletableEntity {

	@Column(nullable = false, length = 200)
	private String titleVi;

	@Column(length = 200)
	private String titleEn;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Level level;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ContentStatus status = ContentStatus.ACTIVE;

	/**
	 * NULL khi người tạo bị xoá cứng. Người tạo bị xoá mềm sẽ bị
	 * {@code @SQLRestriction} của User ẩn, nên chỉ đọc trường này khi thật cần.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;
}
