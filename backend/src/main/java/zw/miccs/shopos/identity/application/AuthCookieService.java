package zw.miccs.shopos.identity.application;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthCookieService {

	private final AuthProperties properties;

	public AuthCookieService(AuthProperties properties) {
		this.properties = properties;
	}

	public void writeSessionCookies(HttpServletResponse response, AuthSessionTokens tokens) {
		write(response, properties.getAccessCookieName(), tokens.accessToken(),
			Duration.between(Instant.now(), tokens.accessExpiresAt()));
		write(response, properties.getRefreshCookieName(), tokens.refreshToken(),
			Duration.between(Instant.now(), tokens.refreshExpiresAt()));
	}

	public void clearSessionCookies(HttpServletResponse response) {
		write(response, properties.getAccessCookieName(), "", Duration.ZERO);
		write(response, properties.getRefreshCookieName(), "", Duration.ZERO);
	}

	public String readCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public String accessCookieName() {
		return properties.getAccessCookieName();
	}

	public String refreshCookieName() {
		return properties.getRefreshCookieName();
	}

	private void write(HttpServletResponse response, String name, String value, Duration maxAge) {
		long seconds = Math.max(maxAge.getSeconds(), 0);
		ResponseCookie cookie = ResponseCookie.from(name, value == null ? "" : value)
			.httpOnly(true)
			.secure(properties.isCookieSecure())
			.path(properties.getCookiePath())
			.sameSite(properties.getCookieSameSite())
			.maxAge(seconds)
			.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}

}
