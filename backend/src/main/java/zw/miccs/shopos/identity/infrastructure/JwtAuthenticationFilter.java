package zw.miccs.shopos.identity.infrastructure;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import zw.miccs.shopos.identity.application.ShoposPrincipal;
import zw.miccs.shopos.tenancy.application.TenantContext;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				String header = request.getHeader(HttpHeaders.AUTHORIZATION);
				if (header != null && header.startsWith("Bearer ")) {
					String token = header.substring(7).trim();
					if (!token.isEmpty()) {
						try {
							ShoposPrincipal principal = jwtService.parseAccessToken(token);
							var authentication = new UsernamePasswordAuthenticationToken(
								principal, null, principal.getAuthorities());
							SecurityContextHolder.getContext().setAuthentication(authentication);
							TenantContext.set(
								principal.tenantId(),
								principal.userId(),
								principal.role(),
								principal.accessibleBranchIds());
						}
						catch (Exception ignored) {
							// Invalid token → leave unauthenticated; security chain returns 401.
						}
					}
				}
			}
			filterChain.doFilter(request, response);
		}
		finally {
			TenantContext.clear();
			SecurityContextHolder.clearContext();
		}
	}
}
