package zw.miccs.shopos.synchronisation.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import zw.miccs.shopos.branches.application.BranchView;
import zw.miccs.shopos.branches.application.DeviceView;
import zw.miccs.shopos.catalogue.application.ProductView;

public record SyncBootstrap(
	UUID tenantId,
	List<BranchView> branches,
	List<DeviceView> devices,
	List<ProductView> products,
	Instant generatedAt
) {
}
