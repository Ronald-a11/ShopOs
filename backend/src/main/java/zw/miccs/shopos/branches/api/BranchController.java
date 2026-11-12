package zw.miccs.shopos.branches.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import zw.miccs.shopos.branches.api.BranchDtos.BranchResponse;
import zw.miccs.shopos.branches.api.BranchDtos.CreateBranchRequest;
import zw.miccs.shopos.branches.api.BranchDtos.DeviceResponse;
import zw.miccs.shopos.branches.api.BranchDtos.RegisterDeviceRequest;
import zw.miccs.shopos.branches.api.BranchDtos.UpdateBranchRequest;
import zw.miccs.shopos.branches.application.BranchService;
import zw.miccs.shopos.branches.application.BranchView;
import zw.miccs.shopos.branches.application.DeviceView;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Branches")
public class BranchController {

	private final BranchService branches;
	private final CurrentTenantService tenants;

	public BranchController(BranchService branches, CurrentTenantService tenants) {
		this.branches = branches;
		this.tenants = tenants;
	}

	@GetMapping("/branches")
	@Operation(summary = "List branches for the current tenant")
	ResponseEntity<List<BranchResponse>> list(Authentication authentication) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(branches.listBranches(tenantId).stream().map(BranchController::toResponse).toList());
	}

	@PostMapping("/branches")
	@Operation(summary = "Create a branch")
	ResponseEntity<BranchResponse> create(Authentication authentication, @Valid @RequestBody CreateBranchRequest request) {
		UUID tenantId = tenantId(authentication);
		BranchView created = branches.create(tenantId, request.code(), request.name(), request.address());
		return ResponseEntity.status(201).body(toResponse(created));
	}

	@GetMapping("/branches/{branchId}")
	@Operation(summary = "Get a branch")
	ResponseEntity<BranchResponse> get(Authentication authentication, @PathVariable UUID branchId) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(toResponse(branches.requireBranch(tenantId, branchId)));
	}

	@PatchMapping("/branches/{branchId}")
	@Operation(summary = "Update branch name, address, or status")
	ResponseEntity<BranchResponse> update(
		Authentication authentication,
		@PathVariable UUID branchId,
		@Valid @RequestBody UpdateBranchRequest request
	) {
		UUID tenantId = tenantId(authentication);
		BranchView updated = branches.update(tenantId, branchId, request.name(), request.address(), request.status());
		return ResponseEntity.ok(toResponse(updated));
	}

	@GetMapping("/branches/{branchId}/devices")
	@Operation(summary = "List tills and POS devices at a branch")
	ResponseEntity<List<DeviceResponse>> listDevices(Authentication authentication, @PathVariable UUID branchId) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(branches.listDevices(tenantId, branchId).stream().map(BranchController::toResponse).toList());
	}

	@PostMapping("/branches/{branchId}/devices")
	@Operation(summary = "Register a till or POS device")
	ResponseEntity<DeviceResponse> registerDevice(
		Authentication authentication,
		@PathVariable UUID branchId,
		@Valid @RequestBody RegisterDeviceRequest request
	) {
		UUID tenantId = tenantId(authentication);
		DeviceView created = branches.registerDevice(
			tenantId, branchId, request.code(), request.name(), request.platform(), null);
		return ResponseEntity.status(201).body(toResponse(created));
	}

	private UUID tenantId(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		return tenants.requirePrimaryTenantId(principal.userId());
	}

	private static BranchResponse toResponse(BranchView view) {
		return new BranchResponse(view.id(), view.code(), view.name(), view.address(), view.status(), view.createdAt());
	}

	private static DeviceResponse toResponse(DeviceView view) {
		return new DeviceResponse(
			view.id(), view.branchId(), view.code(), view.name(), view.platform(), view.status(), view.createdAt());
	}

}
