package vn.enlearning.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/** Entity có thể sửa sau khi tạo, nên theo dõi thêm thời điểm cập nhật. */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditedEntity extends BaseEntity {

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;
}
