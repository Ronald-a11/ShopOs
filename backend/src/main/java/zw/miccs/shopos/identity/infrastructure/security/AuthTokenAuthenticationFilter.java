package zw.miccs.shopos.identity.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import zw.miccs.shopos.identity.application.AuthCookieService;
import zw.miccs.shopos.identity.application.AuthService;

@Component
public class AuthTokenAuthenticationFilter extends OncePerRequestFilter {

	private final AuthService authService;
	private final AuthCookieService cookies;

	public AuthTokenAuthenticationFilter(AuthService authService, AuthCookieService cookies) {
		this.authService = authService;
		this.cookies = cookies;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			String rawToken = extractAccessToken(request);
			authService.resolveAccessToken(rawToken).ifPresent(principal -> {
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_USER")));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			});
		}
		filterChain.doFilter(request, response);
	}

	private String extractAccessToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return header.substring(7).trim();
		}
		return cookies.readCookie(request, cookies.accessCookieName());
	}

}
