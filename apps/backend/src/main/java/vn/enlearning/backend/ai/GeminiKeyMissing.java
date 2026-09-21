package vn.enlearning.backend.ai;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Bản giả chỉ được nạp khi {@code app.ai.gemini-api-key} trống. (Không dùng {@code @ConditionalOnProperty}
 * vì {@code havingValue = ""} nghĩa là "không chỉ định".)
 */
public class GeminiKeyMissing implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		String key = context.getEnvironment().getProperty("app.ai.gemini-api-key", "");
		return key.isBlank();
	}
}
