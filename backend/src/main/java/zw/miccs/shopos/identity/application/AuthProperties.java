package zw.miccs.shopos.identity.application;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shopos.auth")
public class AuthProperties {

	private String jwtSecret = "local-dev-only-change-me-to-a-long-random-secret-key";
	private Duration accessTokenTtl = Duration.ofMinutes(15);
	private Duration refreshTokenTtl = Duration.ofDays(7);
	private Duration refreshTokenTtlRemember = Duration.ofDays(30);
	private String refreshCookieName = "shopos_refresh";
	private boolean refreshCookieSecure = false;

	public String getJwtSecret() {
		return jwtSecret;
	}

	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	public Duration getAccessTokenTtl() {
		return accessTokenTtl;
	}

	public void setAccessTokenTtl(Duration accessTokenTtl) {
		this.accessTokenTtl = accessTokenTtl;
	}

	public Duration getRefreshTokenTtl() {
		return refreshTokenTtl;
	}

	public void setRefreshTokenTtl(Duration refreshTokenTtl) {
		this.refreshTokenTtl = refreshTokenTtl;
	}

	public Duration getRefreshTokenTtlRemember() {
		return refreshTokenTtlRemember;
	}

	public void setRefreshTokenTtlRemember(Duration refreshTokenTtlRemember) {
		this.refreshTokenTtlRemember = refreshTokenTtlRemember;
	}

	public String getRefreshCookieName() {
		return refreshCookieName;
	}

	public void setRefreshCookieName(String refreshCookieName) {
		this.refreshCookieName = refreshCookieName;
	}

	public boolean isRefreshCookieSecure() {
		return refreshCookieSecure;
	}

	public void setRefreshCookieSecure(boolean refreshCookieSecure) {
		this.refreshCookieSecure = refreshCookieSecure;
	}
}
