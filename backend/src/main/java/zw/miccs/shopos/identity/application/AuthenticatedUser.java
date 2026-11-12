package zw.miccs.shopos.identity.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import zw.miccs.shopos.identity.domain.UserStatus;
import zw.miccs.shopos.tenancy.application.MembershipView;

public record AuthenticatedUser(
	UUID id,
	String email,
	String displayName,
	String phone,
	UserStatus status,
	boolean emailVerified,
	boolean phoneVerified,
	Instant createdAt,
	List<MembershipView> memberships
) {
}
