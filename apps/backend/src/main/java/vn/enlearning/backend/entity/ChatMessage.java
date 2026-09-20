package vn.enlearning.backend.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.enlearning.backend.entity.enums.ChatRole;

/**
 * Một tin nhắn trong hội thoại. Không sửa sau khi tạo nên chỉ có thời điểm tạo.
 * {@code links}, {@code refusal}, {@code modelName} và số token chỉ có ở câu trả
 * lời của trợ lý; CHECK ở DB chặn việc đặt chúng cho tin nhắn của người học.
 */
@Getter
@Setter
@Entity
@Table(name = "chat_messages", indexes = {
		@Index(name = "idx_chat_messages_conversation", columnList = "conversation_id, created_at")
})
public class ChatMessage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "conversation_id", nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ChatConversation conversation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ChatRole role;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	/** Gợi ý bài học kèm theo, lưu dạng JSON; thứ tự mảng là thứ tự hiển thị. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "JSON")
	private List<ChatSuggestionLink> links = new ArrayList<>();

	/** Trợ lý từ chối vì câu hỏi nằm ngoài phạm vi học tiếng Anh. */
	@Column(name = "is_refusal", nullable = false)
	private boolean refusal;

	@Column(length = 100)
	private String modelName;

	private Integer promptTokens;

	private Integer completionTokens;
}
