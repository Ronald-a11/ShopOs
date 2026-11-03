package zw.miccs.shopos.inventory.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryBalance(
	UUID branchId,
	UUID productId,
	String sku,
	String productName,
	BigDecimal quantity,
	BigDecimal reorderLevel
) {
}
