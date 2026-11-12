package zw.miccs.shopos.identity.application;

/**
 * Delivers recovery links and verification codes. Local/dev implementation logs only;
 * production swaps in email/SMS providers without changing auth flows.
 */
public interface OutboundNotifier {

	void sendPasswordReset(String email, String rawToken);

	void sendVerificationCode(String destination, String method, String code);

}
