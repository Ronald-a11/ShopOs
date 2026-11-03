package zw.miccs.shopos.inventory.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryMovement(
	UUID id,
	UUID branchId,
	UUID productId,
	MovementType movementType,
	BigDecimal quantityDelta,
	String referenceType,
	UUID referenceId,
	Instant occurredAt
) {
}
