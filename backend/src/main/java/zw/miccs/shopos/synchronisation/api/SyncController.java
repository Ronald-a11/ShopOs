package zw.miccs.shopos.synchronisation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.synchronisation.api.SyncDtos.BootstrapResponse;
import zw.miccs.shopos.synchronisation.api.SyncDtos.BranchSnapshot;
import zw.miccs.shopos.synchronisation.api.SyncDtos.DeviceSnapshot;
import zw.miccs.shopos.synchronisation.api.SyncDtos.ProductSnapshot;
import zw.miccs.shopos.synchronisation.application.SyncBootstrap;
import zw.miccs.shopos.synchronisation.application.SyncService;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Synchronisation")
public class SyncController {

	private final SyncService sync;
	private final CurrentTenantService tenants;

	public SyncController(SyncService sync, CurrentTenantService tenants) {
		this.sync = sync;
		this.tenants = tenants;
	}

	@GetMapping("/sync/bootstrap")
	@Operation(summary = "Offline bootstrap of branches, devices, and products")
	ResponseEntity<BootstrapResponse> bootstrap(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		SyncBootstrap snapshot = sync.bootstrap(tenants.requirePrimaryTenantId(principal.userId()));
		return ResponseEntity.ok(new BootstrapResponse(
			snapshot.tenantId(),
			snapshot.branches().stream()
				.map(branch -> new BranchSnapshot(branch.id(), branch.code(), branch.name(), branch.address(), branch.status()))
				.toList(),
			snapshot.devices().stream()
				.map(device -> new DeviceSnapshot(
					device.id(), device.branchId(), device.code(), device.name(), device.platform(), device.status()))
				.toList(),
			snapshot.products().stream()
				.map(product -> new ProductSnapshot(
					product.id(),
					product.sku(),
					product.name(),
					product.barcode(),
					product.unitPrice(),
					product.taxRateCode(),
					product.status()))
				.toList(),
			snapshot.generatedAt()));
	}

}
