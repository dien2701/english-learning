package vn.enlearning.backend.mail;

public class EmailSendException extends RuntimeException {

	public EmailSendException(String message, Throwable cause) {
		super(message, cause);
	}
}
