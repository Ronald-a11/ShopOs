package zw.miccs.shopos.inventory.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import zw.miccs.shopos.inventory.application.AdjustmentLineInput;
import zw.miccs.shopos.inventory.application.ReceiptLineInput;
import zw.miccs.shopos.inventory.domain.InventoryAdjustmentResult;
import zw.miccs.shopos.inventory.domain.InventoryBalance;
import zw.miccs.shopos.inventory.domain.InventoryMovement;
import zw.miccs.shopos.inventory.domain.InventoryReceiptResult;
import zw.miccs.shopos.inventory.domain.MovementType;

public final class InventoryDtos {

	private InventoryDtos() {
	}

	public record InventoryLineRequest(
		@NotNull UUID productId,
		@NotBlank String quantity
	) {
		ReceiptLineInput toReceiptLine() {
			return new ReceiptLineInput(productId, new BigDecimal(quantity.trim()));
		}
	}

	public record AdjustmentLineRequest(
		@NotNull UUID productId,
		@NotBlank String quantityDelta
	) {
		AdjustmentLineInput toInput() {
			return new AdjustmentLineInput(productId, new BigDecimal(quantityDelta.trim()));
		}
	}

	public record InventoryReceiptRequest(
		@NotNull UUID branchId,
		@NotNull UUID clientOperationId,
		String reason,
		@NotEmpty List<@Valid InventoryLineRequest> lines
	) {
	}

	public record InventoryAdjustmentRequest(
		@NotNull UUID branchId,
		@NotNull UUID clientOperationId,
		@NotBlank String reason,
		MovementType movementType,
		@NotEmpty List<@Valid AdjustmentLineRequest> lines
	) {
	}

	public record InventoryBalanceResponse(
		UUID branchId,
		UUID productId,
		String sku,
		String productName,
		String quantity,
		String reorderLevel
	) {
		static InventoryBalanceResponse from(InventoryBalance balance) {
			return new InventoryBalanceResponse(
				balance.branchId(),
				balance.productId(),
				balance.sku(),
				balance.productName(),
				decimal(balance.quantity()),
				decimal(balance.reorderLevel()));
		}
	}

	public record InventoryMovementResponse(
		UUID id,
		UUID branchId,
		UUID productId,
		MovementType movementType,
		String quantityDelta,
		String referenceType,
		UUID referenceId,
		Instant occurredAt
	) {
		static InventoryMovementResponse from(InventoryMovement movement) {
			return new InventoryMovementResponse(
				movement.id(),
				movement.branchId(),
				movement.productId(),
				movement.movementType(),
				decimal(movement.quantityDelta()),
				movement.referenceType(),
				movement.referenceId(),
				movement.occurredAt());
		}
	}

	public record InventoryReceiptResponse(
		UUID receiptId,
		UUID branchId,
		List<InventoryMovementResponse> movements
	) {
		static InventoryReceiptResponse from(InventoryReceiptResult result) {
			return new InventoryReceiptResponse(
				result.receiptId(),
				result.branchId(),
				result.movements().stream().map(InventoryMovementResponse::from).toList());
		}
	}

	public record InventoryAdjustmentResponse(
		UUID adjustmentId,
		UUID branchId,
		List<InventoryMovementResponse> movements
	) {
		static InventoryAdjustmentResponse from(InventoryAdjustmentResult result) {
			return new InventoryAdjustmentResponse(
				result.adjustmentId(),
				result.branchId(),
				result.movements().stream().map(InventoryMovementResponse::from).toList());
		}
	}

	private static String decimal(BigDecimal value) {
		return value == null ? null : value.stripTrailingZeros().toPlainString();
	}
}
