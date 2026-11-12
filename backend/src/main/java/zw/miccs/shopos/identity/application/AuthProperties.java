package zw.miccs.shopos.identity.application;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shopos.auth")
public class AuthProperties {

	private Duration accessTokenTtl = Duration.ofMinutes(15);
	private Duration refreshTokenTtl = Duration.ofDays(7);
	private Duration rememberRefreshTokenTtl = Duration.ofDays(30);
	private Duration passwordResetTtl = Duration.ofHours(1);
	private Duration verificationCodeTtl = Duration.ofMinutes(10);
	private String accessCookieName = "shopos_at";
	private String refreshCookieName = "shopos_rt";
	private String cookiePath = "/api";
	private boolean cookieSecure = false;
	private String cookieSameSite = "Lax";

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

	public Duration getRememberRefreshTokenTtl() {
		return rememberRefreshTokenTtl;
	}

	public void setRememberRefreshTokenTtl(Duration rememberRefreshTokenTtl) {
		this.rememberRefreshTokenTtl = rememberRefreshTokenTtl;
	}

	public Duration getPasswordResetTtl() {
		return passwordResetTtl;
	}

	public void setPasswordResetTtl(Duration passwordResetTtl) {
		this.passwordResetTtl = passwordResetTtl;
	}

	public Duration getVerificationCodeTtl() {
		return verificationCodeTtl;
	}

	public void setVerificationCodeTtl(Duration verificationCodeTtl) {
		this.verificationCodeTtl = verificationCodeTtl;
	}

	public String getAccessCookieName() {
		return accessCookieName;
	}

	public void setAccessCookieName(String accessCookieName) {
		this.accessCookieName = accessCookieName;
	}

	public String getRefreshCookieName() {
		return refreshCookieName;
	}

	public void setRefreshCookieName(String refreshCookieName) {
		this.refreshCookieName = refreshCookieName;
	}

	public String getCookiePath() {
		return cookiePath;
	}

	public void setCookiePath(String cookiePath) {
		this.cookiePath = cookiePath;
	}

	public boolean isCookieSecure() {
		return cookieSecure;
	}

	public void setCookieSecure(boolean cookieSecure) {
		this.cookieSecure = cookieSecure;
	}

	public String getCookieSameSite() {
		return cookieSameSite;
	}

	public void setCookieSameSite(String cookieSameSite) {
		this.cookieSameSite = cookieSameSite;
	}

}
