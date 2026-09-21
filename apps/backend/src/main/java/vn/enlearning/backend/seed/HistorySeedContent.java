package vn.enlearning.backend.seed;

import java.util.List;

import vn.enlearning.backend.entity.AiFeedbackIssue;
import vn.enlearning.backend.entity.enums.ChatRole;
import vn.enlearning.backend.entity.enums.IssueCategory;
import vn.enlearning.backend.entity.enums.StudySkill;

/**
 * Văn bản mẫu cho lịch sử Luyện viết, Luyện nói và Chat của học viên demo chính (tự soạn, không chép đề có bản quyền).
 * Bài viết gắn với đề theo {@code titleEn} trong {@code seed-demo/writing.json}; đề không có trong danh sách bị bỏ qua.
 * Mọi {@code excerpt} đều là đoạn có thật trong {@code content} để trang phản hồi tô đúng chỗ.
 */
final class HistorySeedContent {

	private HistorySeedContent() {
	}

	/** Bài viết đã nộp; {@code grade} là điểm nền thang 10 (tăng dần theo thời gian). */
	record Essay(String titleEn, int daysAgo, double grade, boolean needsRetry, String content, String summary,
			List<AiFeedbackIssue> issues) {
	}

	/** Xếp từ cũ đến mới: bài được lưu theo thứ tự này nên bài mới nhất có khoá lớn nhất. */
	static final List<Essay> ESSAYS = List.of(
			new Essay("Describe Your Daily Routine", 80, 5.8, false,
					"I wake up at 6 o'clock every morning. First, I brush my teeth and take a shower. Then I eat "
							+ "breakfast with my family, usually bread and a cup of milk. I go to school by bus, it take "
							+ "about thirty minutes. At school, I studies math, English and science. I come home at five "
							+ "and I do my homework. In the evening, I watch TV with my parents and I go to bed at ten. "
							+ "I like my routine because it is simple and I feel very comfortable.",
					"Bài viết đủ ý và có trình tự thời gian rõ ràng. Cần chú ý chia động từ theo chủ ngữ ngôi thứ ba "
							+ "và tránh nối hai mệnh đề chỉ bằng dấu phẩy.",
					List.of(
							new AiFeedbackIssue(IssueCategory.GRAMMAR, "it take about thirty minutes",
									"Chủ ngữ \"it\" cần động từ thêm -s ở thì hiện tại đơn.",
									"it takes about thirty minutes"),
							new AiFeedbackIssue(IssueCategory.GRAMMAR, "I studies math",
									"Chủ ngữ \"I\" dùng động từ nguyên mẫu, không thêm -s/-es.",
									"I study math, English and science"),
							new AiFeedbackIssue(IssueCategory.EXPRESSION, "I go to school by bus, it take",
									"Hai mệnh đề độc lập bị nối chỉ bằng dấu phẩy.",
									"I go to school by bus, and it takes about thirty minutes."))),
			new Essay("Write About Your Family", 62, 6.2, false,
					"There are four people in my family: my father, my mother, my younger brother and me. My father "
							+ "is a engineer and he works for a big company. My mother is a teacher, she teach English at "
							+ "a primary school. My brother is ten years old and he is very funny. On weekends, we often "
							+ "go to the park together or cook dinner at home. I love my family very much because they "
							+ "always support me when I have problems.",
					"Giới thiệu gia đình mạch lạc, câu ngắn dễ hiểu. Lỗi chính là mạo từ và chia động từ; nên dùng từ "
							+ "miêu tả tính cách đa dạng hơn.",
					List.of(
							new AiFeedbackIssue(IssueCategory.GRAMMAR, "a engineer",
									"Trước nguyên âm dùng \"an\" thay vì \"a\".", "an engineer"),
							new AiFeedbackIssue(IssueCategory.GRAMMAR, "she teach English",
									"Chủ ngữ \"she\" cần động từ thêm -s.", "she teaches English"),
							new AiFeedbackIssue(IssueCategory.VOCABULARY, "very funny",
									"\"very funny\" khá chung chung.",
									"Use a more precise word such as \"humorous\" or \"cheerful\"."))),
			new Essay("Email to a Friend About a Trip", 45, 6.7, false,
					"Hi Linh,\n\nI am writing to tell you about my trip to Da Nang last week. We travelled by plane "
							+ "and arrived in the morning. The hotel was near the beach, so we swam every afternoon. The "
							+ "seafood was delicious and not expensive. On the second day we visited Ba Na Hills and take "
							+ "many photos on the Golden Bridge. The weather was hot but we enjoyed it a lot. I hope you "
							+ "can come with us next time.\n\nBest wishes,\nMinh",
					"Email thân mật, bố cục đúng (lời chào, nội dung, lời kết). Cần nhất quán thì quá khứ và chọn từ "
							+ "tự nhiên hơn.",
					List.of(
							new AiFeedbackIssue(IssueCategory.GRAMMAR, "take many photos",
									"Câu kể lại chuyện đã qua nên dùng quá khứ đơn.", "took many photos"),
							new AiFeedbackIssue(IssueCategory.VOCABULARY, "not expensive",
									"Có thể dùng một từ khẳng định thay cho phủ định.", "delicious and affordable"),
							new AiFeedbackIssue(IssueCategory.EXPRESSION, "enjoyed it a lot",
									"Cách nói này đúng nhưng đơn giản.", "had a wonderful time"))),
			new Essay("Email to Reschedule a Meeting", 30, 7.1, false,
					"Dear Mr. Harris,\n\nI hope you are well. I am writing to ask if we can reschedule our meeting "
							+ "planned for Thursday at 10 a.m. Unfortunately, I have to attend an urgent client call at "
							+ "the same time. Would it be possible to move the meeting to Friday morning, or to any time "
							+ "next Monday? I apologise for any inconvenience this may cause and I will adjust my "
							+ "schedule to fit yours. In the meantime, I will send you the agenda and the latest project "
							+ "report so that you can review them in advance, and we can use our time together more "
							+ "effectively. Please let me know which option is convenient for you. I look forward to "
							+ "hear from you.\n\nKind regards,\nAn Nguyen",
					"Email công việc lịch sự, nêu rõ lý do và đưa ra phương án thay thế. Chỉ còn một lỗi cấu trúc ở "
							+ "câu kết.",
					List.of(
							new AiFeedbackIssue(IssueCategory.GRAMMAR, "I look forward to hear from you",
									"Sau \"look forward to\" dùng V-ing.", "I look forward to hearing from you"),
							new AiFeedbackIssue(IssueCategory.EXPRESSION, "I will adjust my schedule to fit yours",
									"Có thể diễn đạt lịch sự và chủ động hơn.",
									"I am happy to adjust my schedule to suit yours."))),
			new Essay("Working from Home: Pros and Cons", 16, 7.4, false,
					"Working from home has become common in many companies, especially in the technology sector. In "
							+ "my opinion, it has both advantages and disadvantages.\n\nOn the one hand, remote work saves "
							+ "a lot of time and money. Employees do not need to spend two hours a day on crowded buses, "
							+ "so they can sleep longer and spend more time with their family. Companies can also reduce "
							+ "the cost of renting large offices.\n\nOn the other hand, working at home can be lonely. "
							+ "Many people find it difficult to separate their job from their private life, so they often "
							+ "works late at night. Communication is another problem, because a quick question in the "
							+ "office becomes a long chat message.\n\nIn conclusion, I believe a hybrid model is the best "
							+ "solution, because it gives workers freedom without losing the benefits of team work.",
					"Bài luận có bố cục rõ (mở bài, hai thân bài đối lập, kết luận) và quan điểm nhất quán. Cần sửa "
							+ "lỗi hoà hợp chủ vị và một số từ ghép.",
					List.of(
							new AiFeedbackIssue(IssueCategory.GRAMMAR, "they often works late at night",
									"Chủ ngữ \"they\" dùng động từ nguyên mẫu.", "they often work late at night"),
							new AiFeedbackIssue(IssueCategory.VOCABULARY, "team work",
									"\"teamwork\" viết liền thành một từ.", "the benefits of teamwork"),
							new AiFeedbackIssue(IssueCategory.EXPRESSION,
									"a quick question in the office becomes a long chat message",
									"Ý hay nhưng chưa giải thích vì sao đó là bất lợi.",
									"a simple question that takes seconds face to face can turn into a long thread"))),
			new Essay("Does Sport Keep You Healthy?", 6, 7.9, false,
					"Many people believe that playing sport is the best way to stay healthy. I largely agree with "
							+ "this view, although it is not the only factor.\n\nFirst of all, regular exercise "
							+ "strengthens the heart and helps control body weight. Doctors recommend at least thirty "
							+ "minutes of moderate activity on most days of the week. Team sports such as football or "
							+ "volleyball also improve mental health, since players make friends and release stress "
							+ "together.\n\nHowever, sport alone is not enough. A person who trains every day but eats "
							+ "fast food and sleeps only five hours will still face health problems. Balanced nutrition "
							+ "and enough sleep are equally important.\n\nTo sum up, sport plays a vital role in a "
							+ "healthy lifestyle, but it should be combined with good eating habits and rest.",
					"Lập luận cân bằng, có ví dụ cụ thể và từ nối tốt. Chỉ còn vài lựa chọn từ chưa thật chính xác.",
					List.of(
							new AiFeedbackIssue(IssueCategory.VOCABULARY, "release stress",
									"Collocation thông dụng là \"relieve stress\".", "relieve stress together"),
							new AiFeedbackIssue(IssueCategory.EXPRESSION, "will still face health problems",
									"Nên dùng cấu trúc diễn đạt khả năng thay vì khẳng định tuyệt đối.",
									"is still likely to suffer from health problems"))),
			new Essay("Your Favourite Food", 1, 7.5, true,
					"My favourite food is pho, a traditional Vietnamese noodle soup. My grandmother cooks it every "
							+ "Sunday with beef bones, ginger and many fresh herbs. The broth is clear and sweet, and the "
							+ "noodles are soft. I usually add a little lime juice and some chilli sauce. When I eat pho "
							+ "with my family, I feel warm and happy. It is cheap and you can find it in every city. I "
							+ "think everyone should try it once.",
					"", List.of()));

	/** Nhận xét ngắn theo mức điểm của một câu nói. */
	static String speakingComment(double score) {
		if (score >= 8.0) {
			return "Phát âm rõ ràng, tốc độ tự nhiên.";
		}
		if (score >= 6.5) {
			return "Khá tốt, chú ý thêm trọng âm của những từ dài.";
		}
		return "Cần luyện thêm cách nối âm và ngữ điệu của câu.";
	}

	static final List<String> SPEAKING_IMPROVEMENTS = List.of(
			"Chú ý âm cuối (-s, -ed) khi nói.",
			"Ngắt câu tự nhiên hơn, tránh dừng giữa một cụm từ.",
			"Nhấn trọng âm vào từ khoá của câu.",
			"Mở rộng câu trả lời bằng một ví dụ cụ thể.");

	/** Một tin nhắn trong hội thoại mẫu; {@code skills} chỉ có ở câu trả lời của trợ lý. */
	record ChatTurn(ChatRole role, String content, List<StudySkill> skills, boolean refusal) {

		static ChatTurn user(String content) {
			return new ChatTurn(ChatRole.USER, content, List.of(), false);
		}

		static ChatTurn assistant(String content, StudySkill... skills) {
			return new ChatTurn(ChatRole.ASSISTANT, content, List.of(skills), false);
		}

		static ChatTurn refusal(String content) {
			return new ChatTurn(ChatRole.ASSISTANT, content, List.of(), true);
		}
	}

	record Chat(int daysAgo, List<ChatTurn> turns) {
	}

	/** Cũ đến mới; hội thoại cuối diễn ra "hôm nay" nên nằm đầu danh sách trong giao diện. */
	static final List<Chat> CHATS = List.of(
			new Chat(30, List.of(
					ChatTurn.user("Mình nên luyện nghe tiếng Anh như thế nào cho hiệu quả?"),
					ChatTurn.assistant("Để cải thiện kỹ năng nghe, hãy nghe cùng một đoạn ba lần: lần đầu nắm ý chính, "
							+ "lần hai bắt chi tiết như số liệu và tên riêng, lần ba vừa nghe vừa đọc transcript để "
							+ "đối chiếu. Mỗi ngày 15-20 phút đều đặn hiệu quả hơn học dồn vào cuối tuần.",
							StudySkill.LISTENING),
					ChatTurn.user("Còn phát âm thì luyện thế nào?"),
					ChatTurn.assistant("Bạn nên nghe một câu mẫu, đọc lại thành tiếng rồi so sánh với bản gốc. Chú ý "
							+ "trọng âm của từ và các âm cuối như -s, -ed. Bài Luyện nói có chấm điểm sẽ chỉ ra những "
							+ "từ bạn phát âm chưa chuẩn.", StudySkill.SPEAKING))),
			new Chat(14, List.of(
					ChatTurn.user("Khi nào dùng thì hiện tại hoàn thành thay vì quá khứ đơn?"),
					ChatTurn.assistant("Dùng hiện tại hoàn thành (have/has + V3) khi hành động bắt đầu trong quá khứ và "
							+ "còn liên quan tới hiện tại, hoặc khi không nói rõ thời điểm: \"I have lived here for five "
							+ "years.\" Dùng quá khứ đơn khi có mốc thời gian đã qua: \"I lived there in 2019.\""),
					ChatTurn.user("Cho mình thêm ví dụ với just, already và yet nhé."),
					ChatTurn.assistant("Ví dụ: \"I have just finished my homework.\" (vừa mới xong); \"She has already "
							+ "left.\" (đã rời đi rồi); \"Have you eaten yet?\" (đã ăn chưa, dùng trong câu hỏi và câu "
							+ "phủ định)."))),
			new Chat(5, List.of(
					ChatTurn.user("Gợi ý cho mình cách học từ vựng TOEIC nhanh"),
					ChatTurn.assistant("Hãy học từ theo chủ đề (văn phòng, du lịch, hợp đồng) thay vì học rời rạc. Mỗi "
							+ "ngày học 10-15 từ bằng flashcard, ôn lại theo lịch giãn cách và đặt ít nhất một câu ví "
							+ "dụ cho mỗi từ. Đọc các bài đọc ngắn cũng giúp bạn gặp lại từ trong ngữ cảnh.",
							StudySkill.VOCABULARY, StudySkill.READING))),
			new Chat(0, List.of(
					ChatTurn.user("Hôm nay thời tiết ở Hà Nội thế nào?"),
					ChatTurn.refusal("Mình chỉ hỗ trợ các câu hỏi liên quan tới việc học tiếng Anh thôi nhé. Bạn có "
							+ "thể hỏi mình về từ vựng, ngữ pháp, phát âm, hoặc nhờ mình gợi ý bài luyện tập phù hợp."),
					ChatTurn.user("Vậy sửa giúp mình câu này: I go to school yesterday"),
					ChatTurn.assistant("Câu đúng là \"I went to school yesterday.\" Vì có trạng từ chỉ quá khứ "
							+ "\"yesterday\" nên động từ phải chia ở quá khứ đơn: go → went."))));
}
