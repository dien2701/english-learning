package vn.enlearning.backend.common;

/** Vượt giới hạn tần suất; {@link GlobalExceptionHandler} trả 429 kèm header {@code Retry-After}. */
public class RateLimitedException extends ApiException {

	private final long retryAfterSeconds;

	public RateLimitedException(long retryAfterSeconds) {
		super(ErrorCode.RATE_LIMITED);
		this.retryAfterSeconds = retryAfterSeconds;
	}

	public long getRetryAfterSeconds() {
		return retryAfterSeconds;
	}
}
