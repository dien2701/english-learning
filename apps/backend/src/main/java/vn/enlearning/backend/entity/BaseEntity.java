package vn.enlearning.backend.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Gốc của mọi entity: khoá UUID v7 tự sinh (sắp theo thời gian nên chèn
 * vào chỉ mục B-tree ít phân mảnh hơn UUID v4) và thời điểm tạo.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@GeneratedValue
	@UuidGenerator(style = UuidGenerator.Style.VERSION_7)
	@Column(nullable = false, updatable = false)
	private UUID id;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
			return false;
		}
		UUID otherId = ((BaseEntity) other).id;
		return id != null && id.equals(otherId);
	}

	@Override
	public int hashCode() {
		return Hibernate.getClass(this).hashCode();
	}
}
