package zw.miccs.shopos.inventory.api;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.inventory.api.InventoryDtos.InventoryAdjustmentRequest;
import zw.miccs.shopos.inventory.api.InventoryDtos.InventoryAdjustmentResponse;
import zw.miccs.shopos.inventory.api.InventoryDtos.InventoryBalanceResponse;
import zw.miccs.shopos.inventory.api.InventoryDtos.InventoryMovementResponse;
import zw.miccs.shopos.inventory.api.InventoryDtos.InventoryReceiptRequest;
import zw.miccs.shopos.inventory.api.InventoryDtos.InventoryReceiptResponse;
import zw.miccs.shopos.inventory.application.InventoryService;
import zw.miccs.shopos.inventory.domain.InventoryAdjustmentResult;
import zw.miccs.shopos.inventory.domain.InventoryBalance;
import zw.miccs.shopos.inventory.domain.InventoryMovement;
import zw.miccs.shopos.inventory.domain.InventoryReceiptResult;
import zw.miccs.shopos.inventory.domain.MovementType;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

	private final InventoryService inventoryService;

	public InventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping("/receipts")
	ResponseEntity<InventoryReceiptResponse> receiveGoods(@Valid @RequestBody InventoryReceiptRequest request) {
		InventoryReceiptResult result = inventoryService.receiveGoods(
			request.branchId(),
			request.clientOperationId(),
			request.reason(),
			request.lines().stream().map(InventoryDtos.InventoryLineRequest::toReceiptLine).toList());
		HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(InventoryReceiptResponse.from(result));
	}

	@PostMapping("/adjustments")
	ResponseEntity<InventoryAdjustmentResponse> adjustInventory(
		@Valid @RequestBody InventoryAdjustmentRequest request) {
		MovementType type = request.movementType() == null ? MovementType.ADJUSTMENT : request.movementType();
		InventoryAdjustmentResult result = inventoryService.adjustStock(
			request.branchId(),
			request.clientOperationId(),
			request.reason(),
			type,
			request.lines().stream().map(InventoryDtos.AdjustmentLineRequest::toInput).toList());
		HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(InventoryAdjustmentResponse.from(result));
	}

	@GetMapping("/movements")
	PageResponse<InventoryMovementResponse> listMovements(
		@RequestParam UUID branchId,
		@RequestParam(required = false) UUID productId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort) {
		PageResponse<InventoryMovement> result = inventoryService.listMovements(
			branchId,
			productId,
			toPageable(page, size, sort, "occurredAt"));
		return new PageResponse<>(
			result.content().stream().map(InventoryMovementResponse::from).toList(),
			result.page());
	}

	@GetMapping("/low-stock")
	PageResponse<InventoryBalanceResponse> listLowStock(
		@RequestParam UUID branchId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort) {
		PageResponse<InventoryBalance> result = inventoryService.listLowStock(
			branchId,
			toPageable(page, size, sort, "productId"));
		return new PageResponse<>(
			result.content().stream().map(InventoryBalanceResponse::from).toList(),
			result.page());
	}

	private static Pageable toPageable(int page, int size, String sort, String defaultProperty) {
		int safeSize = Math.min(Math.max(size, 1), 200);
		int safePage = Math.max(page, 0);
		if (sort == null || sort.isBlank()) {
			return PageRequest.of(safePage, safeSize, Sort.by(defaultProperty).descending());
		}
		String[] parts = sort.split(",");
		String property = parts[0].trim();
		Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
			? Sort.Direction.DESC
			: Sort.Direction.ASC;
		return PageRequest.of(safePage, safeSize, Sort.by(direction, property));
	}
}
