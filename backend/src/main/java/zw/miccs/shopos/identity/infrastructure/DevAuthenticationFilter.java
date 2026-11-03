package zw.miccs.shopos.identity.infrastructure;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import zw.miccs.shopos.identity.application.ShoposPrincipal;
import zw.miccs.shopos.tenancy.application.TenantContext;
import zw.miccs.shopos.tenancy.domain.Role;

/**
 * Local/test auth only. When X-Tenant-Id headers are present and no auth is set yet
 * (e.g. no Bearer token), authenticates from headers for branch ITs.
 */
public class DevAuthenticationFilter extends OncePerRequestFilter {

	public static final String TENANT_HEADER = "X-Tenant-Id";
	public static final String USER_HEADER = "X-User-Id";
	public static final String ROLE_HEADER = "X-Role";
	public static final String BRANCHES_HEADER = "X-Branch-Ids";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String tenantHeader = request.getHeader(TENANT_HEADER);
		String userHeader = request.getHeader(USER_HEADER);
		String roleHeader = request.getHeader(ROLE_HEADER);
		if (SecurityContextHolder.getContext().getAuthentication() == null
			&& tenantHeader != null && userHeader != null && roleHeader != null) {
			UUID tenantId = UUID.fromString(tenantHeader);
			UUID userId = UUID.fromString(userHeader);
			Role role = Role.valueOf(roleHeader);
			Set<UUID> branchIds = parseBranches(request.getHeader(BRANCHES_HEADER), role);
			ShoposPrincipal principal = new ShoposPrincipal(userId, tenantId, userId + "@dev.local", role, branchIds);
			var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			TenantContext.set(tenantId, userId, role, branchIds);
		}
		filterChain.doFilter(request, response);
	}

	private static Set<UUID> parseBranches(String header, Role role) {
		if (header == null || header.isBlank()) {
			if (role.canReadAllBranchesWhenUnscoped()) {
				return null;
			}
			return Set.of();
		}
		return Arrays.stream(header.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.map(UUID::fromString)
			.collect(Collectors.toUnmodifiableSet());
	}
}
