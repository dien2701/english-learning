package vn.enlearning.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Một cuộc hội thoại giữa người học và trợ lý AI. Đoạn xem trước và số tin nhắn
 * của danh sách hội thoại không lưu ở đây mà tính khi đọc từ {@link ChatMessage}.
 * Xoá là xoá mềm: tin nhắn giữ lại để đối soát chi phí AI.
 */
@Getter
@Setter
@Entity
@Table(name = "chat_conversations", indexes = {
		@Index(name = "idx_chat_conversations_user", columnList = "user_id, last_message_at")
})
@SQLDelete(sql = "UPDATE chat_conversations SET deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ChatConversation extends SoftDeletableEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private User user;

	/** Backend đặt từ tin nhắn đầu tiên. */
	@Column(nullable = false, length = 200)
	private String title;

	/** Backend cập nhật mỗi khi có tin nhắn mới, dùng sắp danh sách hội thoại. */
	@Column(nullable = false)
	private Instant lastMessageAt;
}
