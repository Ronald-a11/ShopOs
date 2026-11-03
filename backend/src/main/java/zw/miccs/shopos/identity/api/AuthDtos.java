package zw.miccs.shopos.identity.api;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import zw.miccs.shopos.identity.application.AuthModels.AuthResult;
import zw.miccs.shopos.identity.application.AuthModels.MeView;
import zw.miccs.shopos.identity.application.AuthModels.MembershipView;
import zw.miccs.shopos.tenancy.domain.Role;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String password,
		UUID deviceId,
		Boolean keepSignedIn
	) {
	}

	public record RefreshRequest(String refreshToken) {
	}

	public record MembershipResponse(
		UUID tenantId,
		String tenantName,
		Role role,
		List<UUID> branchIds
	) {
		static MembershipResponse from(MembershipView view) {
			return new MembershipResponse(view.tenantId(), view.tenantName(), view.role(), view.branchIds());
		}
	}

	public record MeResponse(
		UUID id,
		String email,
		String displayName,
		List<MembershipResponse> memberships
	) {
		static MeResponse from(MeView view) {
			return new MeResponse(
				view.id(),
				view.email(),
				view.displayName(),
				view.memberships().stream().map(MembershipResponse::from).toList());
		}
	}

	public record LoginResponse(
		String accessToken,
		long expiresIn,
		String tokenType,
		String refreshToken,
		MeResponse user
	) {
		static LoginResponse from(AuthResult result) {
			return new LoginResponse(
				result.accessToken(),
				result.expiresIn(),
				"Bearer",
				result.refreshToken(),
				MeResponse.from(result.user()));
		}
	}
}
