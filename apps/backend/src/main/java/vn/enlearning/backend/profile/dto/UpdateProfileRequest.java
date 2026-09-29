package vn.enlearning.backend.profile.dto;

/** Trường null là giữ nguyên; {@code phoneNumber} rỗng là xoá giá trị. Đổi ảnh đại diện dùng {@code POST /profile/avatar}. */
public record UpdateProfileRequest(String fullName, String email, String phoneNumber) {
}
