package vn.enlearning.backend.common;

import java.util.UUID;

/** Kết quả {@code GROUP BY} đếm theo một khoá UUID (thường là id người dùng). */
public interface IdCount {

	UUID getId();

	long getTotal();
}
