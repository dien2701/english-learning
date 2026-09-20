package vn.enlearning.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Dữ liệu quan trọng chỉ bị đánh dấu xoá. Mỗi entity con tự khai báo
 * {@code @SQLDelete} và {@code @SQLRestriction} vì hai annotation này
 * cần tên bảng riêng.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditedEntity {

	@Column
	private Instant deletedAt;

	public boolean isDeleted() {
		return deletedAt != null;
	}
}
