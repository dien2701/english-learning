package vn.enlearning.backend.audio;

/**
 * Cổng đọc văn bản thành giọng nói. {@link OpenAiSpeechSynthesizer} khi có {@code app.audio.openai-api-key},
 * ngược lại {@link FakeSpeechSynthesizer}.
 */
public interface SpeechSynthesizer {

	/**
	 * @param text  đoạn cần đọc, đã cắt ngắn hơn giới hạn của API
	 * @param voice tên giọng của OpenAI ({@code alloy}, {@code echo}, ...)
	 * @return byte MP3
	 * @throws SpeechSynthesisException khi không nhận được audio hợp lệ
	 */
	byte[] synthesize(String text, String voice);
}
