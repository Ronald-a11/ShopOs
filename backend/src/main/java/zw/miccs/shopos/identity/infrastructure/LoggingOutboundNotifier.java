package zw.miccs.shopos.identity.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import zw.miccs.shopos.identity.application.OutboundNotifier;

/**
 * Development notifier. Tokens/codes are logged for local testing only — never use in production.
 */
@Component
public class LoggingOutboundNotifier implements OutboundNotifier {

	private static final Logger log = LoggerFactory.getLogger(LoggingOutboundNotifier.class);

	@Override
	public void sendPasswordReset(String email, String rawToken) {
		log.info("Password reset token for {} (dev only): {}", email, rawToken);
	}

	@Override
	public void sendVerificationCode(String destination, String method, String code) {
		log.info("Verification code for {} via {} (dev only): {}", destination, method, code);
	}

}
