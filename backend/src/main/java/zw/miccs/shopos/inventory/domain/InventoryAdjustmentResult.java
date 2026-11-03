package zw.miccs.shopos.inventory.domain;

import java.util.List;
import java.util.UUID;

public record InventoryAdjustmentResult(
	UUID adjustmentId,
	UUID branchId,
	List<InventoryMovement> movements,
	boolean replayed
) {
}
