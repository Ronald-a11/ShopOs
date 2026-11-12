package zw.miccs.shopos.tenancy.application;

import java.time.Instant;
import java.util.UUID;

import zw.miccs.shopos.tenancy.domain.TenantRole;

/**
 * Public tenancy write interface used by other modules to provision memberships.
 */
public interface MembershipProvisioningService {

	void createActiveTenantAndMembership(UUID userId, String tenantName, TenantRole role, Instant createdAt);

}
