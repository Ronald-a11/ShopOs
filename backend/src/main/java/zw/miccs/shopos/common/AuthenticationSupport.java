package zw.miccs.shopos.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;

public final class AuthenticationSupport {

	private AuthenticationSupport() {
	}

	public static AuthenticatedPrincipal requirePrincipal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Unauthenticated",
				"Authentication is required");
		}
		return principal;
	}

}
