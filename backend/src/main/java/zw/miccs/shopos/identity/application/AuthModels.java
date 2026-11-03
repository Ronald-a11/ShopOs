package zw.miccs.shopos.identity.application;

import java.util.List;
import java.util.UUID;

import zw.miccs.shopos.tenancy.domain.Role;

public final class AuthModels {

	private AuthModels() {
	}

	public record MembershipView(
		UUID tenantId,
		String tenantName,
		Role role,
		List<UUID> branchIds
	) {
	}

	public record MeView(
		UUID id,
		String email,
		String displayName,
		List<MembershipView> memberships
	) {
	}

	public record AuthResult(
		String accessToken,
		long expiresIn,
		String refreshToken,
		MeView user,
		ShoposPrincipal principal
	) {
	}
}
