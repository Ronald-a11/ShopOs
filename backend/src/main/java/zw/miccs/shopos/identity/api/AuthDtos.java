package zw.miccs.shopos.identity.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import zw.miccs.shopos.identity.domain.UserStatus;
import zw.miccs.shopos.identity.domain.VerificationMethod;
import zw.miccs.shopos.tenancy.domain.TenantRole;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String password,
		boolean rememberMe
	) {
	}

	public record ForgotPasswordRequest(
		@NotBlank @Email String email
	) {
	}

	public record ResetPasswordRequest(
		@NotBlank String token,
		@NotBlank @Size(min = 8, max = 128) String password
	) {
	}

	public record SendVerificationRequest(
		@NotNull VerificationMethod method,
		@NotBlank String value
	) {
	}

	public record ConfirmVerificationRequest(
		@NotNull VerificationMethod method,
		@NotBlank String value,
		@NotBlank @Size(min = 4, max = 12) String code
	) {
	}

	public record MembershipResponse(
		UUID membershipId,
		UUID tenantId,
		String tenantName,
		TenantRole role
	) {
	}

	public record UserResponse(
		UUID id,
		String email,
		String displayName,
		String phone,
		UserStatus status,
		boolean emailVerified,
		boolean phoneVerified,
		Instant createdAt,
		List<MembershipResponse> memberships
	) {
	}

	public record AuthResponse(
		UserResponse user
	) {
	}

}
