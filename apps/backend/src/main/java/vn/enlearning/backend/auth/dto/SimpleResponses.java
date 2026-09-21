package vn.enlearning.backend.auth.dto;

/** Các phản hồi một trường của module auth. */
public final class SimpleResponses {

	private SimpleResponses() {
	}

	public record EmailAvailability(boolean available) {
	}

	public record LoggedOut(boolean loggedOut) {
	}

	public record Message(String message) {
	}
}
