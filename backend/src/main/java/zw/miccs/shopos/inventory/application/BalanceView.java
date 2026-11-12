package zw.miccs.shopos.inventory.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BalanceView(
	UUID branchId,
	UUID productId,
	String sku,
	String name,
	BigDecimal quantity,
	Instant updatedAt
) {
}
