package vn.enlearning.backend.ai;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/** Ngược của {@link GeminiKeyMissing}: bản Gemini thật chỉ được nạp khi đã có {@code app.ai.gemini-api-key}. */
public class GeminiKeyPresent implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return !new GeminiKeyMissing().matches(context, metadata);
	}
}
