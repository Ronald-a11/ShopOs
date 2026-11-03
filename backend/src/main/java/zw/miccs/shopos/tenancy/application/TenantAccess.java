package zw.miccs.shopos.tenancy.application;

import java.util.UUID;

/**
 * Public tenancy facade for other modules. Never expose repositories across modules.
 */
public interface TenantAccess {

	UUID requireTenantId();

	boolean canAccessBranch(UUID branchId);

	void requireBranch(UUID branchId);

	boolean canManageBranches();
}
