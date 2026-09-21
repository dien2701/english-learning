package vn.enlearning.backend.admin.dto;

import java.util.UUID;

import vn.enlearning.backend.common.L10n;

/** {@code itemCount}: số nội dung (kể cả INACTIVE) đang thuộc chủ đề; chủ đề còn nội dung không xoá được. */
public record AdminTopicResponse(UUID id, String slug, L10n name, long itemCount) {
}
