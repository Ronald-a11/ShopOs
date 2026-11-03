package zw.miccs.shopos.identity.infrastructure;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import zw.miccs.shopos.identity.application.AuthProperties;

/**
 * Fails fast outside local/test when JWT secret is missing or still the default.
 */
@Component
public class AuthSecretValidator implements ApplicationRunner {

	static final String DEFAULT_DEV_SECRET = "local-dev-only-change-me-to-a-long-random-secret-key";

	private final AuthProperties authProperties;
	private final Environment environment;

	public AuthSecretValidator(AuthProperties authProperties, Environment environment) {
		this.authProperties = authProperties;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (isLocalOrTest()) {
			return;
		}
		String secret = authProperties.getJwtSecret();
		if (secret == null || secret.isBlank() || DEFAULT_DEV_SECRET.equals(secret) || secret.length() < 32) {
			throw new IllegalStateException(
				"shopos.auth.jwt-secret must be set to a strong secret (min 32 chars) outside local/test profiles");
		}
		if (!authProperties.isRefreshCookieSecure()) {
			throw new IllegalStateException(
				"shopos.auth.refresh-cookie-secure must be true outside local/test profiles");
		}
	}

	private boolean isLocalOrTest() {
		for (String profile : environment.getActiveProfiles()) {
			if ("local".equals(profile) || "test".equals(profile)) {
				return true;
			}
		}
		return environment.getActiveProfiles().length == 0;
	}
}
