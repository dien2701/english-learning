package vn.enlearning.backend.profile.dto;

/** Trường null là giữ nguyên; {@code phoneNumber}/{@code avatarUrl} rỗng là xoá giá trị. */
public record UpdateProfileRequest(String fullName, String email, String phoneNumber, String avatarUrl) {
}
