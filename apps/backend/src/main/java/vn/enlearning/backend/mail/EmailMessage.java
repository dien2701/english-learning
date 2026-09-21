package vn.enlearning.backend.mail;

/** Một email đã dựng xong, gồm bản văn bản thuần và bản HTML. */
public record EmailMessage(String to, String subject, String text, String html) {
}
