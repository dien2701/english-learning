package vn.enlearning.backend.admin.dto;

import vn.enlearning.backend.entity.enums.AccountStatus;
import vn.enlearning.backend.entity.enums.Role;

/** Trường null là giữ nguyên. {@code status} chỉ nhận ACTIVE (mở khoá) hoặc LOCKED (khoá). */
public record UpdateUserRequest(AccountStatus status, Role role) {
}
