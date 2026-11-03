package zw.miccs.shopos.inventory.domain;

import java.util.List;
import java.util.UUID;

public record InventoryReceiptResult(
	UUID receiptId,
	UUID branchId,
	List<InventoryMovement> movements,
	boolean replayed
) {
}
