package zw.miccs.shopos.tenancy.domain;

public enum Role {
	OWNER,
	ADMIN,
	MANAGER,
	SUPERVISOR,
	CASHIER,
	STOCK_CONTROLLER;

	public boolean canManageBranches() {
		return this == OWNER || this == ADMIN;
	}

	public boolean canManageCatalogue() {
		return this == OWNER || this == ADMIN;
	}

	public boolean canReceiveStock() {
		return this == OWNER || this == ADMIN || this == MANAGER || this == STOCK_CONTROLLER;
	}

	public boolean canAdjustStock() {
		return this == OWNER || this == ADMIN || this == MANAGER;
	}

	public boolean canRecordWastage() {
		return canAdjustStock() || this == STOCK_CONTROLLER;
	}

	public boolean canOperateShift() {
		return this == OWNER || this == ADMIN || this == MANAGER || this == SUPERVISOR || this == CASHIER;
	}

	public boolean canApproveShiftVariance() {
		return this == OWNER || this == ADMIN || this == MANAGER || this == SUPERVISOR;
	}

	public boolean canReadAllBranchesWhenUnscoped() {
		return this == OWNER || this == ADMIN || this == MANAGER;
	}
}
