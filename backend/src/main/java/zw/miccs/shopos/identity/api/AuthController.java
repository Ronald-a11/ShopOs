package zw.miccs.shopos.identity.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import zw.miccs.shopos.identity.api.AuthDtos.LoginRequest;
import zw.miccs.shopos.identity.api.AuthDtos.LoginResponse;
import zw.miccs.shopos.identity.api.AuthDtos.RefreshRequest;
import zw.miccs.shopos.identity.application.AuthModels.AuthResult;
import zw.miccs.shopos.identity.application.AuthProperties;
import zw.miccs.shopos.identity.application.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final AuthProperties authProperties;

	public AuthController(AuthService authService, AuthProperties authProperties) {
		this.authService = authService;
		this.authProperties = authProperties;
	}

	@PostMapping("/login")
	ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		boolean keepSignedIn = Boolean.TRUE.equals(request.keepSignedIn());
		AuthResult result = authService.login(request.email(), request.password(), request.deviceId(), keepSignedIn);
		return withRefreshCookie(LoginResponse.from(result), result.refreshToken(), keepSignedIn);
	}

	@PostMapping("/refresh")
	ResponseEntity<LoginResponse> refresh(
		@RequestBody(required = false) RefreshRequest request,
		HttpServletRequest httpRequest) {
		AuthResult result = authService.refresh(resolveRefreshToken(request, httpRequest));
		return withRefreshCookie(LoginResponse.from(result), result.refreshToken(), false);
	}

	@PostMapping("/logout")
	ResponseEntity<Void> logout(
		@RequestBody(required = false) RefreshRequest request,
		HttpServletRequest httpRequest) {
		authService.logout(resolveRefreshToken(request, httpRequest));
		ResponseCookie clear = ResponseCookie.from(authProperties.getRefreshCookieName(), "")
			.httpOnly(true)
			.secure(authProperties.isRefreshCookieSecure())
			.path("/api/v1/auth")
			.maxAge(0)
			.sameSite("Lax")
			.build();
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clear.toString()).build();
	}

	private ResponseEntity<LoginResponse> withRefreshCookie(LoginResponse body, String refreshToken, boolean keepSignedIn) {
		long maxAge = keepSignedIn
			? authProperties.getRefreshTokenTtlRemember().toSeconds()
			: authProperties.getRefreshTokenTtl().toSeconds();
		ResponseCookie cookie = ResponseCookie.from(authProperties.getRefreshCookieName(), refreshToken)
			.httpOnly(true)
			.secure(authProperties.isRefreshCookieSecure())
			.path("/api/v1/auth")
			.maxAge(maxAge)
			.sameSite("Lax")
			.build();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);
	}

	private String resolveRefreshToken(RefreshRequest request, HttpServletRequest httpRequest) {
		if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
			return request.refreshToken();
		}
		Cookie[] cookies = httpRequest.getCookies();
		if (cookies == null) {
			return null;
		}
		String name = authProperties.getRefreshCookieName();
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
