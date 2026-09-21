package vn.enlearning.backend.ratelimit;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import vn.enlearning.backend.common.RateLimitedException;

/**
 * Giới hạn tần suất bằng bucket4j đặt trong bộ nhớ (một tiến trình; nâng lên nhiều máy thì đổi sang
 * bucket4j có backend phân tán mà vẫn giữ giao diện này). Mỗi luật có một bộ nhớ đệm Caffeine giữ bucket
 * theo khoá; bucket nhàn rỗi quá một {@code window} đã đầy lại nên được bỏ đi, bộ nhớ không phình ra.
 */
@Component
public class RateLimiter {

	/** Các luật, gắn với endpoint ở {@link RateLimitConfig}. */
	public enum Rule { LOGIN, CHECK_EMAIL, FORGOT_PASSWORD }

	private static final long MAX_KEYS = 100_000;

	private final boolean enabled;
	private final Map<Rule, RateLimitProperties.Rule> settings = new EnumMap<>(Rule.class);
	private final Map<Rule, Cache<String, Bucket>> buckets = new EnumMap<>(Rule.class);

	RateLimiter(RateLimitProperties properties) {
		this.enabled = properties.enabled();
		settings.put(Rule.LOGIN, properties.login());
		settings.put(Rule.CHECK_EMAIL, properties.checkEmail());
		settings.put(Rule.FORGOT_PASSWORD, properties.forgotPassword());
		settings.forEach((rule, setting) -> buckets.put(rule, Caffeine.newBuilder()
				.maximumSize(MAX_KEYS)
				.expireAfterAccess(setting.window())
				.build()));
	}

	/** @throws RateLimitedException khi {@code key} đã dùng hết lượt của luật trong cửa sổ hiện tại */
	public void consume(Rule rule, String key) {
		if (!enabled) {
			return;
		}
		RateLimitProperties.Rule setting = settings.get(rule);
		Bucket bucket = buckets.get(rule).get(key, k -> newBucket(setting));
		ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
		if (!probe.isConsumed()) {
			long seconds = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds() + 1;
			throw new RateLimitedException(seconds);
		}
	}

	/** Xoá mọi bộ đếm; dùng cho test. */
	public void reset() {
		buckets.values().forEach(Cache::invalidateAll);
	}

	private static Bucket newBucket(RateLimitProperties.Rule setting) {
		return Bucket.builder()
				.addLimit(Bandwidth.builder()
						.capacity(setting.capacity())
						.refillIntervally(setting.capacity(), setting.window())
						.build())
				.build();
	}
}
