package vn.enlearning.backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vn.enlearning.backend.ai.ChatAssistant;
import vn.enlearning.backend.entity.ChatMessage;
import vn.enlearning.backend.entity.enums.ChatRole;

class ChatContextTest {

	private static ChatMessage message(String content) {
		ChatMessage m = new ChatMessage();
		m.setRole(ChatRole.USER);
		m.setContent(content);
		return m;
	}

	@Test
	@DisplayName("Ngữ cảnh: đảo về cũ nhất trước, cắt mỗi tin ở 1000 ký tự")
	void ordersOldestFirstAndTruncates() {
		List<ChatAssistant.Turn> turns = ChatService.context(List.of(message("c"), message("b"),
				message("a".repeat(5000))));

		assertThat(turns).extracting(ChatAssistant.Turn::content).containsExactly("a".repeat(1000), "b", "c");
	}

	@Test
	@DisplayName("Ngữ cảnh: quá 6000 ký tự thì bỏ các tin cũ nhất")
	void dropsOldestBeyondTotalBudget() {
		List<ChatMessage> recent = new ArrayList<>();
		for (int i = 9; i >= 0; i--) {
			recent.add(message(i + "x".repeat(999)));
		}

		List<ChatAssistant.Turn> turns = ChatService.context(recent);

		assertThat(turns).hasSize(6);
		assertThat(turns.get(0).content()).startsWith("4");
		assertThat(turns.get(5).content()).startsWith("9");
	}
}
