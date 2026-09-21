package vn.enlearning.backend.dashboard.dto;

import java.util.Locale;

import vn.enlearning.backend.common.ApiException;
import vn.enlearning.backend.common.ErrorCode;

/** Kỳ của biểu đồ thời gian học; frontend gửi {@code WEEK}/{@code MONTH}, chấp nhận cả chữ thường. */
public enum StudyPeriod {
	WEEK, MONTH;

	/** Trống nghĩa là tuần; giá trị lạ là 400 chứ không phải 500. */
	public static StudyPeriod parse(String value) {
		if (value == null || value.isBlank()) {
			return WEEK;
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw ApiException.field(ErrorCode.VALIDATION, "period", "errors.badRequest");
		}
	}
}
