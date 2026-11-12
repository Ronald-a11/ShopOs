package zw.miccs.shopos.inventory.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class InventoryDtos {

	private InventoryDtos() {
	}

	public record ReceiptLineRequest(
		@NotNull UUID productId,
		@NotNull @Positive BigDecimal quantity
	) {
	}

	public record CreateReceiptRequest(
		@NotNull UUID branchId,
		@NotEmpty List<@Valid ReceiptLineRequest> lines,
		@Size(max = 500) String note
	) {
	}

	public record CreateAdjustmentRequest(
		@NotNull UUID branchId,
		@NotNull UUID productId,
		@NotNull BigDecimal quantityDelta,
		@NotBlank @Size(max = 500) String reason
	) {
	}

	public record BalanceResponse(
		UUID branchId,
		UUID productId,
		String sku,
		String name,
		BigDecimal quantity,
		Instant updatedAt
	) {
	}

	public record MovementResponse(
		UUID id,
		UUID branchId,
		UUID productId,
		BigDecimal quantityDelta,
		String movementType,
		String reason,
		String note,
		UUID referenceId,
		Instant createdAt
	) {
	}

}
