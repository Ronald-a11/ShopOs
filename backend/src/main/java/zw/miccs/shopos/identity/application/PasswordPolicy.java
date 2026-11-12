package zw.miccs.shopos.identity.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import zw.miccs.shopos.common.ApiException;

@Component
public class PasswordPolicy {

	public void validate(String password) {
		if (password == null || password.length() < 8) {
			fail("Password must be at least 8 characters");
		}
		if (!password.chars().anyMatch(Character::isUpperCase)) {
			fail("Password must include an uppercase letter");
		}
		if (!password.chars().anyMatch(Character::isDigit)) {
			fail("Password must include a number");
		}
		if (!password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:',.<>/?`~\"\\".indexOf(ch) >= 0)) {
			fail("Password must include a special character");
		}
	}

	private static void fail(String detail) {
		throw new ApiException(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "Weak password", detail);
	}

}
