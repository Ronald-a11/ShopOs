package zw.miccs.shopos.identity.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import zw.miccs.shopos.identity.api.AuthDtos.AuthResponse;
import zw.miccs.shopos.identity.api.AuthDtos.ConfirmVerificationRequest;
import zw.miccs.shopos.identity.api.AuthDtos.ForgotPasswordRequest;
import zw.miccs.shopos.identity.api.AuthDtos.LoginRequest;
import zw.miccs.shopos.identity.api.AuthDtos.MembershipResponse;
import zw.miccs.shopos.identity.api.AuthDtos.ResetPasswordRequest;
import zw.miccs.shopos.identity.api.AuthDtos.SendVerificationRequest;
import zw.miccs.shopos.identity.api.AuthDtos.UserResponse;
import zw.miccs.shopos.identity.application.AuthCookieService;
import zw.miccs.shopos.identity.application.AuthService;
import zw.miccs.shopos.identity.application.AuthSessionTokens;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.identity.application.AuthenticatedUser;
import zw.miccs.shopos.identity.application.LoginResult;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Identity")
public class AuthController {

	private final AuthService authService;
	private final AuthCookieService cookies;

	public AuthController(AuthService authService, AuthCookieService cookies) {
		this.authService = authService;
		this.cookies = cookies;
	}

	@PostMapping("/auth/login")
	@Operation(summary = "Authenticate with email and password")
	ResponseEntity<AuthResponse> login(
		@Valid @RequestBody LoginRequest request,
		HttpServletRequest httpRequest,
		HttpServletResponse httpResponse
	) {
		LoginResult result = authService.login(
			request.email(),
			request.password(),
			request.rememberMe(),
			httpRequest.getHeader("User-Agent"),
			httpRequest.getRemoteAddr());
		cookies.writeSessionCookies(httpResponse, result.tokens());
		return ResponseEntity.ok(new AuthResponse(toUserResponse(result.user())));
	}

	@PostMapping("/auth/refresh")
	@Operation(summary = "Rotate access and refresh tokens")
	ResponseEntity<Void> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		String refreshToken = cookies.readCookie(httpRequest, cookies.refreshCookieName());
		AuthSessionTokens tokens = authService.refresh(
			refreshToken,
			httpRequest.getHeader("User-Agent"),
			httpRequest.getRemoteAddr());
		cookies.writeSessionCookies(httpResponse, tokens);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/logout")
	@Operation(summary = "Revoke the current session")
	ResponseEntity<Void> logout(
		Authentication authentication,
		HttpServletRequest httpRequest,
		HttpServletResponse httpResponse
	) {
		if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
			authService.logout(principal.sessionId());
		}
		else {
			authService.logoutByRefreshToken(cookies.readCookie(httpRequest, cookies.refreshCookieName()));
		}
		cookies.clearSessionCookies(httpResponse);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	@Operation(summary = "Current user and memberships")
	ResponseEntity<UserResponse> me(Authentication authentication) {
		AuthenticatedPrincipal principal = requirePrincipal(authentication);
		return ResponseEntity.ok(toUserResponse(authService.me(principal.userId())));
	}

	@PostMapping("/auth/forgot-password")
	@Operation(summary = "Request a password reset link")
	ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		authService.forgotPassword(request.email());
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/auth/reset-password")
	@Operation(summary = "Reset password with a recovery token")
	ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		authService.resetPassword(request.token(), request.password());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/verification/send")
	@Operation(summary = "Send an email or phone verification code")
	ResponseEntity<Void> sendVerification(@Valid @RequestBody SendVerificationRequest request) {
		authService.sendVerification(request.method(), request.value());
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/auth/verification/confirm")
	@Operation(summary = "Confirm an email or phone verification code")
	ResponseEntity<Void> confirmVerification(@Valid @RequestBody ConfirmVerificationRequest request) {
		authService.confirmVerification(request.method(), request.value(), request.code());
		return ResponseEntity.noContent().build();
	}

	private static AuthenticatedPrincipal requirePrincipal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
			throw new org.springframework.security.authentication.BadCredentialsException("Unauthenticated");
		}
		return principal;
	}

	private static UserResponse toUserResponse(AuthenticatedUser user) {
		return new UserResponse(
			user.id(),
			user.email(),
			user.displayName(),
			user.phone(),
			user.status(),
			user.emailVerified(),
			user.phoneVerified(),
			user.createdAt(),
			user.memberships().stream()
				.map(m -> new MembershipResponse(m.membershipId(), m.tenantId(), m.tenantName(), m.role()))
				.toList());
	}

}
