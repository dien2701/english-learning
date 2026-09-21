package vn.enlearning.backend.dashboard.dto;

import vn.enlearning.backend.common.L10n;

/** Khớp {@code StudyTimePoint} ở frontend. */
public record StudyTimePointResponse(L10n label, long minutes, long previousMinutes) {
}
