package zw.miccs.shopos.identity.application;

import java.time.Instant;
import java.util.UUID;

public record AuthSessionTokens(
	UUID sessionId,
	String accessToken,
	String refreshToken,
	Instant accessExpiresAt,
	Instant refreshExpiresAt,
	boolean rememberMe
) {
}
