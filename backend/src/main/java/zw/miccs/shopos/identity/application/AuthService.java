package zw.miccs.shopos.identity.application;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.common.ApiException;
import zw.miccs.shopos.identity.domain.UserStatus;
import zw.miccs.shopos.identity.domain.VerificationMethod;
import zw.miccs.shopos.identity.infrastructure.persistence.AuthSessionEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.AuthSessionJpaRepository;
import zw.miccs.shopos.identity.infrastructure.persistence.PasswordResetTokenEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.PasswordResetTokenJpaRepository;
import zw.miccs.shopos.identity.infrastructure.persistence.UserCredentialEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.UserCredentialJpaRepository;
import zw.miccs.shopos.identity.infrastructure.persistence.UserEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.UserJpaRepository;
import zw.miccs.shopos.identity.infrastructure.persistence.VerificationChallengeEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.VerificationChallengeJpaRepository;
import zw.miccs.shopos.tenancy.application.MembershipQueryService;
import zw.miccs.shopos.tenancy.application.MembershipView;

@Service
public class AuthService {

	private final UserJpaRepository users;
	private final UserCredentialJpaRepository credentials;
	private final AuthSessionJpaRepository sessions;
	private final PasswordResetTokenJpaRepository passwordResetTokens;
	private final VerificationChallengeJpaRepository verificationChallenges;
	private final MembershipQueryService memberships;
	private final PasswordEncoder passwordEncoder;
	private final TokenHasher tokenHasher;
	private final AuthProperties authProperties;
	private final PasswordPolicy passwordPolicy;
	private final OutboundNotifier notifier;

	public AuthService(
		UserJpaRepository users,
		UserCredentialJpaRepository credentials,
		AuthSessionJpaRepository sessions,
		PasswordResetTokenJpaRepository passwordResetTokens,
		VerificationChallengeJpaRepository verificationChallenges,
		MembershipQueryService memberships,
		PasswordEncoder passwordEncoder,
		TokenHasher tokenHasher,
		AuthProperties authProperties,
		PasswordPolicy passwordPolicy,
		OutboundNotifier notifier
	) {
		this.users = users;
		this.credentials = credentials;
		this.sessions = sessions;
		this.passwordResetTokens = passwordResetTokens;
		this.verificationChallenges = verificationChallenges;
		this.memberships = memberships;
		this.passwordEncoder = passwordEncoder;
		this.tokenHasher = tokenHasher;
		this.authProperties = authProperties;
		this.passwordPolicy = passwordPolicy;
		this.notifier = notifier;
	}

	@Transactional
	public LoginResult login(String email, String password, boolean rememberMe, String userAgent, String ipAddress) {
		String normalizedEmail = normalizeEmail(email);
		UserEntity user = users.findByEmail(normalizedEmail)
			.orElseThrow(this::invalidCredentials);

		assertLoginAllowed(user);

		UserCredentialEntity credential = credentials.findById(user.getId())
			.orElseThrow(this::invalidCredentials);
		if (!passwordEncoder.matches(password, credential.getPasswordHash())) {
			throw invalidCredentials();
		}

		AuthSessionTokens tokens = createSession(user.getId(), rememberMe, userAgent, ipAddress);
		return new LoginResult(toAuthenticatedUser(user), tokens);
	}

	@Transactional
	public AuthSessionTokens refresh(String rawRefreshToken, String userAgent, String ipAddress) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw unauthenticated("Refresh token is missing");
		}

		AuthSessionEntity session = sessions
			.findByRefreshTokenHashAndRevokedAtIsNull(tokenHasher.hash(rawRefreshToken))
			.orElseThrow(() -> unauthenticated("Refresh token is invalid"));

		Instant now = Instant.now();
		if (session.getRefreshExpiresAt().isBefore(now)) {
			session.setRevokedAt(now);
			throw unauthenticated("Refresh token has expired");
		}

		UserEntity user = users.findById(session.getUserId())
			.orElseThrow(() -> unauthenticated("Session user no longer exists"));
		assertLoginAllowed(user);

		String accessToken = tokenHasher.generateToken();
		String refreshToken = tokenHasher.generateToken();
		Instant accessExpiresAt = now.plus(authProperties.getAccessTokenTtl());
		Instant refreshExpiresAt = now.plus(session.isRememberMe()
			? authProperties.getRememberRefreshTokenTtl()
			: authProperties.getRefreshTokenTtl());

		session.setAccessTokenHash(tokenHasher.hash(accessToken));
		session.setRefreshTokenHash(tokenHasher.hash(refreshToken));
		session.setAccessExpiresAt(accessExpiresAt);
		session.setRefreshExpiresAt(refreshExpiresAt);
		session.setLastUsedAt(now);

		return new AuthSessionTokens(
			session.getId(),
			accessToken,
			refreshToken,
			accessExpiresAt,
			refreshExpiresAt,
			session.isRememberMe());
	}

	@Transactional
	public void logout(UUID sessionId) {
		if (sessionId == null) {
			return;
		}
		sessions.findById(sessionId).ifPresent(session -> {
			if (session.getRevokedAt() == null) {
				session.setRevokedAt(Instant.now());
			}
		});
	}

	@Transactional
	public void logoutByRefreshToken(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}
		sessions.findByRefreshTokenHashAndRevokedAtIsNull(tokenHasher.hash(rawRefreshToken))
			.ifPresent(session -> session.setRevokedAt(Instant.now()));
	}

	@Transactional(readOnly = true)
	public AuthenticatedUser me(UUID userId) {
		UserEntity user = users.findById(userId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found",
				"Current user was not found"));
		return toAuthenticatedUser(user);
	}

	@Transactional(readOnly = true)
	public Optional<AuthenticatedPrincipal> resolveAccessToken(String rawAccessToken) {
		if (rawAccessToken == null || rawAccessToken.isBlank()) {
			return Optional.empty();
		}
		return sessions.findByAccessTokenHashAndRevokedAtIsNull(tokenHasher.hash(rawAccessToken))
			.filter(session -> session.getAccessExpiresAt().isAfter(Instant.now()))
			.flatMap(session -> users.findById(session.getUserId())
				.filter(user -> UserStatus.DISABLED.name().equals(user.getStatus()) == false)
				.map(user -> new AuthenticatedPrincipal(session.getId(), user.getId(), user.getEmail())));
	}

	@Transactional
	public void forgotPassword(String email) {
		String normalizedEmail = normalizeEmail(email);
		users.findByEmail(normalizedEmail).ifPresent(user -> {
			String rawToken = tokenHasher.generateToken();
			passwordResetTokens.save(new PasswordResetTokenEntity(
				UUID.randomUUID(),
				user.getId(),
				tokenHasher.hash(rawToken),
				Instant.now().plus(authProperties.getPasswordResetTtl()),
				Instant.now()));
			notifier.sendPasswordReset(user.getEmail(), rawToken);
		});
	}

	@Transactional
	public void resetPassword(String rawToken, String newPassword) {
		passwordPolicy.validate(newPassword);
		PasswordResetTokenEntity token = passwordResetTokens
			.findByTokenHashAndUsedAtIsNull(tokenHasher.hash(rawToken))
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN",
				"Invalid reset token", "The password reset link is invalid or has already been used"));

		if (token.getExpiresAt().isBefore(Instant.now())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "EXPIRED_RESET_TOKEN", "Expired reset token",
				"The password reset link has expired");
		}

		UserCredentialEntity credential = credentials.findById(token.getUserId())
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RESET_TOKEN",
				"Invalid reset token", "The password reset link is invalid"));

		credential.setPasswordHash(passwordEncoder.encode(newPassword));
		credential.setPasswordUpdatedAt(Instant.now());
		token.setUsedAt(Instant.now());

		sessions.findByUserIdAndRevokedAtIsNull(token.getUserId())
			.forEach(session -> session.setRevokedAt(Instant.now()));
	}

	@Transactional
	public void sendVerification(VerificationMethod method, String value) {
		String destination = normalizeDestination(method, value);
		UserEntity user = findUserForVerification(method, destination)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found",
				"No account matches that " + method.name().toLowerCase(Locale.ROOT)));

		String code = tokenHasher.generateNumericCode(6);
		verificationChallenges.save(new VerificationChallengeEntity(
			UUID.randomUUID(),
			user.getId(),
			method.name(),
			destination,
			tokenHasher.hash(code),
			Instant.now().plus(authProperties.getVerificationCodeTtl()),
			Instant.now()));
		notifier.sendVerificationCode(destination, method.name(), code);
	}

	@Transactional
	public void confirmVerification(VerificationMethod method, String value, String code) {
		String destination = normalizeDestination(method, value);
		VerificationChallengeEntity challenge = verificationChallenges
			.findFirstByMethodAndDestinationAndConsumedAtIsNullOrderByCreatedAtDesc(method.name(), destination)
			.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE",
				"Invalid verification code", "No active verification challenge was found"));

		if (challenge.getExpiresAt().isBefore(Instant.now())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "EXPIRED_VERIFICATION_CODE",
				"Expired verification code", "The verification code has expired");
		}
		if (challenge.getAttempts() >= 5) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "VERIFICATION_LOCKED",
				"Verification locked", "Too many invalid verification attempts");
		}
		if (!tokenHasher.hash(code).equals(challenge.getCodeHash())) {
			challenge.setAttempts(challenge.getAttempts() + 1);
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VERIFICATION_CODE",
				"Invalid verification code", "The verification code is incorrect");
		}

		UserEntity user = users.findById(challenge.getUserId())
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found",
				"Account for verification was not found"));

		Instant now = Instant.now();
		if (method == VerificationMethod.EMAIL) {
			user.setEmailVerifiedAt(now);
		}
		else {
			user.setPhoneVerifiedAt(now);
		}
		if (UserStatus.PENDING_VERIFICATION.name().equals(user.getStatus())
			&& user.getEmailVerifiedAt() != null) {
			user.setStatus(UserStatus.ACTIVE.name());
		}
		user.setUpdatedAt(now);
		challenge.setConsumedAt(now);
	}

	private AuthSessionTokens createSession(UUID userId, boolean rememberMe, String userAgent, String ipAddress) {
		Instant now = Instant.now();
		String accessToken = tokenHasher.generateToken();
		String refreshToken = tokenHasher.generateToken();
		Instant accessExpiresAt = now.plus(authProperties.getAccessTokenTtl());
		Instant refreshExpiresAt = now.plus(rememberMe
			? authProperties.getRememberRefreshTokenTtl()
			: authProperties.getRefreshTokenTtl());

		AuthSessionEntity session = new AuthSessionEntity(
			UUID.randomUUID(),
			userId,
			tokenHasher.hash(accessToken),
			tokenHasher.hash(refreshToken),
			rememberMe,
			accessExpiresAt,
			refreshExpiresAt,
			now,
			now,
			userAgent,
			ipAddress);
		sessions.save(session);

		return new AuthSessionTokens(
			session.getId(),
			accessToken,
			refreshToken,
			accessExpiresAt,
			refreshExpiresAt,
			rememberMe);
	}

	private AuthenticatedUser toAuthenticatedUser(UserEntity user) {
		List<MembershipView> membershipViews = memberships.findActiveMembershipsForUser(user.getId());
		return new AuthenticatedUser(
			user.getId(),
			user.getEmail(),
			user.getDisplayName(),
			user.getPhone(),
			UserStatus.valueOf(user.getStatus()),
			user.getEmailVerifiedAt() != null,
			user.getPhoneVerifiedAt() != null,
			user.getCreatedAt() == null ? Instant.now() : user.getCreatedAt(),
			membershipViews);
	}

	private void assertLoginAllowed(UserEntity user) {
		if (UserStatus.DISABLED.name().equals(user.getStatus())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Account disabled",
				"Your account has been disabled and can't be used to sign in right now");
		}
		if (UserStatus.PENDING_VERIFICATION.name().equals(user.getStatus())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_UNVERIFIED", "Account unverified",
				"Verify your email or phone before signing in");
		}
	}

	private Optional<UserEntity> findUserForVerification(VerificationMethod method, String destination) {
		return method == VerificationMethod.EMAIL
			? users.findByEmail(destination)
			: users.findByPhone(destination);
	}

	private static String normalizeEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"email must not be blank");
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static String normalizeDestination(VerificationMethod method, String value) {
		if (value == null || value.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"value must not be blank");
		}
		String trimmed = value.trim();
		return method == VerificationMethod.EMAIL ? trimmed.toLowerCase(Locale.ROOT) : trimmed;
	}

	private ApiException invalidCredentials() {
		return new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials",
			"Email or password is incorrect");
	}

	private static ApiException unauthenticated(String detail) {
		return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Unauthenticated", detail);
	}

}
