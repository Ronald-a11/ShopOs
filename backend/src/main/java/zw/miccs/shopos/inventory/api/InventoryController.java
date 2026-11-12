package zw.miccs.shopos.inventory.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.inventory.api.InventoryDtos.BalanceResponse;
import zw.miccs.shopos.inventory.api.InventoryDtos.CreateAdjustmentRequest;
import zw.miccs.shopos.inventory.api.InventoryDtos.CreateReceiptRequest;
import zw.miccs.shopos.inventory.api.InventoryDtos.MovementResponse;
import zw.miccs.shopos.inventory.application.BalanceView;
import zw.miccs.shopos.inventory.application.InventoryService;
import zw.miccs.shopos.inventory.application.MovementView;
import zw.miccs.shopos.inventory.application.StockLine;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Inventory")
public class InventoryController {

	private final InventoryService inventory;
	private final CurrentTenantService tenants;

	public InventoryController(InventoryService inventory, CurrentTenantService tenants) {
		this.inventory = inventory;
		this.tenants = tenants;
	}

	@GetMapping("/branches/{branchId}/inventory")
	@Operation(summary = "List on-hand balances for a branch")
	ResponseEntity<List<BalanceResponse>> balances(Authentication authentication, @PathVariable UUID branchId) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(inventory.listBalances(tenantId, branchId).stream()
			.map(InventoryController::toResponse)
			.toList());
	}

	@PostMapping("/inventory/receipts")
	@Operation(summary = "Receive stock into a branch")
	ResponseEntity<List<BalanceResponse>> receive(
		Authentication authentication,
		@Valid @RequestBody CreateReceiptRequest request
	) {
		UUID tenantId = tenantId(authentication);
		List<StockLine> lines = request.lines().stream()
			.map(line -> new StockLine(line.productId(), line.quantity()))
			.toList();
		List<BalanceResponse> updated = inventory.receive(tenantId, request.branchId(), lines, request.note()).stream()
			.map(InventoryController::toResponse)
			.toList();
		return ResponseEntity.status(201).body(updated);
	}

	@PostMapping("/inventory/adjustments")
	@Operation(summary = "Adjust on-hand quantity")
	ResponseEntity<BalanceResponse> adjust(
		Authentication authentication,
		@Valid @RequestBody CreateAdjustmentRequest request
	) {
		UUID tenantId = tenantId(authentication);
		BalanceView updated = inventory.adjust(
			tenantId, request.branchId(), request.productId(), request.quantityDelta(), request.reason());
		return ResponseEntity.status(201).body(toResponse(updated));
	}

	@GetMapping("/inventory/movements")
	@Operation(summary = "List stock movements for a branch")
	ResponseEntity<List<MovementResponse>> movements(
		Authentication authentication,
		@RequestParam UUID branchId
	) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(inventory.listMovements(tenantId, branchId).stream()
			.map(InventoryController::toResponse)
			.toList());
	}

	@GetMapping("/inventory/low-stock")
	@Operation(summary = "List balances at or below a threshold")
	ResponseEntity<List<BalanceResponse>> lowStock(
		Authentication authentication,
		@RequestParam(required = false) UUID branchId,
		@RequestParam(required = false) BigDecimal threshold
	) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(inventory.listLowStock(tenantId, branchId, threshold).stream()
			.map(InventoryController::toResponse)
			.toList());
	}

	private UUID tenantId(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		return tenants.requirePrimaryTenantId(principal.userId());
	}

	private static BalanceResponse toResponse(BalanceView view) {
		return new BalanceResponse(
			view.branchId(), view.productId(), view.sku(), view.name(), view.quantity(), view.updatedAt());
	}

	private static MovementResponse toResponse(MovementView view) {
		return new MovementResponse(
			view.id(),
			view.branchId(),
			view.productId(),
			view.quantityDelta(),
			view.movementType(),
			view.reason(),
			view.note(),
			view.referenceId(),
			view.createdAt());
	}

}
