package vn.enlearning.backend.audio.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.enlearning.backend.audio.AudioFiles;
import vn.enlearning.backend.audio.AudioStorage;
import vn.enlearning.backend.audio.AudioStorageException;
import vn.enlearning.backend.audio.Mp3;
import vn.enlearning.backend.audio.SpeechSynthesisException;
import vn.enlearning.backend.audio.SpeechSynthesizer;
import vn.enlearning.backend.audio.StoredAudio;
import vn.enlearning.backend.audio.TranscriptVoicePlanner;
import vn.enlearning.backend.audio.TranscriptVoicePlanner.Utterance;
import vn.enlearning.backend.audio.dto.ListeningAudioResponse;
import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;
import vn.enlearning.backend.content.repository.ListeningLessonRepository;
import vn.enlearning.backend.entity.ListeningLesson;
import vn.enlearning.backend.entity.enums.AudioSource;

/**
 * Audio của bài nghe: sinh từ transcript bằng TTS đa giọng, tải file lên, hoặc xoá. Việc gọi mạng (TTS, lưu trữ)
 * nằm ngoài giao dịch DB; giao dịch chỉ bao lần cập nhật cuối. File mới luôn có tên mới (URL đổi theo, không dính đệm),
 * file cũ chỉ bị xoá sau khi cập nhật DB đã commit.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningAudioService {

	private final ListeningLessonRepository lessons;
	private final AudioStorage storage;
	private final SpeechSynthesizer synthesizer;
	private final TransactionTemplate tx;

	/**
	 * Đọc transcript bằng {@link TranscriptVoicePlanner} (mỗi người nói một giọng), ghép thành MP3 rồi thay audio hiện có.
	 *
	 * @throws ApiException {@code AI_UNAVAILABLE} khi TTS lỗi, {@code STORAGE_UNAVAILABLE} khi lưu file lỗi
	 */
	public ListeningAudioResponse generate(UUID id) {
		String transcript = tx.execute(s -> find(id).getTranscript());
		List<Utterance> plan = TranscriptVoicePlanner.plan(transcript);
		if (plan.isEmpty()) {
			throw ApiException.field(ErrorCode.VALIDATION, "transcript", "errors.field.transcriptRequired");
		}
		Mp3.Audio audio = synthesize(id, plan);
		return replace(id, audio.frames(), "mp3", AudioSource.TTS, Math.max(1, (int) Math.round(audio.seconds())));
	}

	/** Thay audio bằng file Admin tải lên (mp3, m4a, wav; tối đa {@link AudioFiles#MAX_UPLOAD_BYTES}). */
	public ListeningAudioResponse upload(UUID id, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw ApiException.field(ErrorCode.VALIDATION, "file", "errors.field.fileRequired");
		}
		String extension = AudioFiles.extensionFor(file.getContentType());
		if (extension == null) {
			throw new ApiException(ErrorCode.AUDIO_UNSUPPORTED);
		}
		if (file.getSize() > AudioFiles.MAX_UPLOAD_BYTES) {
			throw new ApiException(ErrorCode.AUDIO_TOO_LARGE);
		}
		byte[] data;
		try {
			data = file.getBytes();
		} catch (IOException e) {
			throw new IllegalStateException("Không đọc được file tải lên", e);
		}
		int seconds = AudioFiles.probeSeconds(extension, data);
		if (seconds < 0) {
			throw new ApiException(ErrorCode.AUDIO_UNSUPPORTED);
		}
		tx.executeWithoutResult(s -> find(id)); // 404 trước khi lưu, để không sinh file mồ côi
		return replace(id, data, extension, AudioSource.UPLOAD, seconds);
	}

	/** Bỏ audio: bài quay lại dùng TTS của trình duyệt. Thời lượng giữ nguyên. */
	public ListeningAudioResponse remove(UUID id) {
		Applied applied = tx.execute(s -> {
			ListeningLesson lesson = find(id);
			String previous = lesson.getAudioPublicId();
			lesson.setAudioUrl(null);
			lesson.setAudioSource(null);
			lesson.setAudioPublicId(null);
			lessons.saveAndFlush(lesson);
			return new Applied(response(lesson), previous);
		});
		discardAfterCommit(applied.previousPublicId());
		return applied.response();
	}

	// --- nội bộ -----------------------------------------------------------------------------------

	private record Applied(ListeningAudioResponse response, String previousPublicId) {
	}

	private Mp3.Audio synthesize(UUID id, List<Utterance> plan) {
		try {
			List<byte[]> parts = new ArrayList<>();
			for (Utterance u : plan) {
				parts.add(synthesizer.synthesize(u.text(), u.voice()));
			}
			return Mp3.join(parts);
		} catch (SpeechSynthesisException | IllegalArgumentException e) {
			log.warn("Sinh audio cho bài nghe {} lỗi: {}", id, e.getMessage());
			throw new ApiException(ErrorCode.AI_UNAVAILABLE);
		}
	}

	/** {@code seconds <= 0} nghĩa là không đo được: giữ thời lượng cũ. */
	private ListeningAudioResponse replace(UUID id, byte[] data, String extension, AudioSource source, int seconds) {
		StoredAudio stored;
		try {
			stored = storage.store(data, id + "-" + System.currentTimeMillis(), extension);
		} catch (AudioStorageException e) {
			log.warn("Lưu audio bài nghe {} lỗi: {}", id, e.getMessage());
			throw new ApiException(ErrorCode.STORAGE_UNAVAILABLE);
		}
		Applied applied;
		try {
			applied = tx.execute(s -> {
				ListeningLesson lesson = find(id);
				String previous = lesson.getAudioPublicId();
				lesson.setAudioUrl(stored.url());
				lesson.setAudioSource(source);
				lesson.setAudioPublicId(stored.publicId());
				if (seconds > 0) {
					lesson.setDurationSeconds(seconds);
				}
				lessons.saveAndFlush(lesson);
				return new Applied(response(lesson), previous);
			});
		} catch (RuntimeException e) {
			storage.delete(stored.publicId());
			throw e;
		}
		discardAfterCommit(applied.previousPublicId());
		return applied.response();
	}

	private ListeningLesson find(UUID id) {
		return lessons.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
	}

	private static ListeningAudioResponse response(ListeningLesson lesson) {
		return new ListeningAudioResponse(lesson.getId(), lesson.getAudioUrl(), lesson.getAudioSource(),
				lesson.getDurationSeconds());
	}

	/** Xoá file cũ sau khi DB commit; nếu đang nằm trong giao dịch ngoài (ví dụ test) thì đợi giao dịch đó. */
	private void discardAfterCommit(String publicId) {
		if (publicId == null) {
			return;
		}
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					storage.delete(publicId);
				}
			});
		} else {
			storage.delete(publicId);
		}
	}
}
