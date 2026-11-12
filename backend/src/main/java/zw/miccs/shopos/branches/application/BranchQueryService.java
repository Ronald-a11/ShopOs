package zw.miccs.shopos.branches.application;

import java.util.List;
import java.util.UUID;

/**
 * Public branch and device lookups. Other modules must not read branch tables directly.
 */
public interface BranchQueryService {

	BranchView requireBranch(UUID tenantId, UUID branchId);

	List<BranchView> listBranches(UUID tenantId);

	List<DeviceView> listDevices(UUID tenantId, UUID branchId);

	DeviceView requireDevice(UUID tenantId, UUID deviceId);

}
