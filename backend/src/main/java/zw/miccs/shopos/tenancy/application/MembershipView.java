package zw.miccs.shopos.tenancy.application;

import java.util.UUID;

import zw.miccs.shopos.tenancy.domain.TenantRole;

public record MembershipView(
	UUID membershipId,
	UUID tenantId,
	String tenantName,
	TenantRole role
) {
}
