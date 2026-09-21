package vn.enlearning.backend.common;

/** Chuỗi song ngữ, khớp kiểu {@code L10n} của frontend. Thiếu bản tiếng Anh thì lùi về tiếng Việt. */
public record L10n(String vi, String en) {

	public static L10n of(String vi, String en) {
		return new L10n(vi, en == null || en.isBlank() ? vi : en);
	}
}
