package zw.miccs.shopos.synchronisation.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.application.BranchQueryService;
import zw.miccs.shopos.branches.application.BranchView;
import zw.miccs.shopos.branches.application.DeviceView;
import zw.miccs.shopos.catalogue.application.CatalogueQueryService;
import zw.miccs.shopos.catalogue.application.ProductView;

@Service
public class SyncService {

	private final BranchQueryService branches;
	private final CatalogueQueryService catalogue;

	public SyncService(BranchQueryService branches, CatalogueQueryService catalogue) {
		this.branches = branches;
		this.catalogue = catalogue;
	}

	@Transactional(readOnly = true)
	public SyncBootstrap bootstrap(UUID tenantId) {
		List<BranchView> branchViews = branches.listBranches(tenantId);
		List<DeviceView> devices = new ArrayList<>();
		for (BranchView branch : branchViews) {
			devices.addAll(branches.listDevices(tenantId, branch.id()));
		}
		List<ProductView> products = catalogue.searchProducts(tenantId, null, null);
		return new SyncBootstrap(tenantId, branchViews, devices, products, Instant.now());
	}

}
