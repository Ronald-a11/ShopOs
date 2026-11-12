package zw.miccs.shopos.identity.application;

import java.util.UUID;

public record AuthenticatedPrincipal(
	UUID sessionId,
	UUID userId,
	String email
) {
}
