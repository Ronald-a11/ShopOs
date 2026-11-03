package zw.miccs.shopos.tenancy.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import zw.miccs.shopos.common.api.NotFoundException;

@Service
public class TenantAccessService implements TenantAccess {

	@Override
	public UUID requireTenantId() {
		return TenantContext.requireTenantId();
	}

	@Override
	public boolean canAccessBranch(UUID branchId) {
		TenantContext.Holder holder = TenantContext.require();
		if (holder.allBranchesAccessible()) {
			return true;
		}
		return holder.branchIdsOrEmpty().contains(branchId);
	}

	@Override
	public void requireBranch(UUID branchId) {
		if (!canAccessBranch(branchId)) {
			// Indistinguishable from missing to avoid cross-tenant leaks.
			throw new NotFoundException("BRANCH_NOT_FOUND", "Branch not found");
		}
	}

	@Override
	public boolean canManageBranches() {
		return TenantContext.require().role().canManageBranches();
	}
}
