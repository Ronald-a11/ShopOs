package zw.miccs.shopos.inventory.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementView(
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
