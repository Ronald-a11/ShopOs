package zw.miccs.shopos.inventory.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.inventory.domain.InventoryAdjustmentResult;
import zw.miccs.shopos.inventory.domain.InventoryBalance;
import zw.miccs.shopos.inventory.domain.InventoryMovement;
import zw.miccs.shopos.inventory.domain.InventoryReceiptResult;
import zw.miccs.shopos.inventory.domain.MovementType;

public interface InventoryService {

	PageResponse<InventoryBalance> listBalances(UUID branchId, Pageable pageable);

	PageResponse<InventoryMovement> listMovements(UUID branchId, UUID productId, Pageable pageable);

	PageResponse<InventoryBalance> listLowStock(UUID branchId, Pageable pageable);

	InventoryReceiptResult receiveGoods(
		UUID branchId,
		UUID clientOperationId,
		String reason,
		List<ReceiptLineInput> lines);

	InventoryAdjustmentResult adjustStock(
		UUID branchId,
		UUID clientOperationId,
		String reason,
		MovementType movementType,
		List<AdjustmentLineInput> lines);

	/**
	 * Apply a signed stock change and append a movement. Used by receive/adjust and
	 * reserved for the sales module to post SALE/RETURN without touching repositories.
	 */
	InventoryMovement applyStockChange(
		UUID branchId,
		UUID productId,
		MovementType movementType,
		BigDecimal quantityDelta,
		String referenceType,
		UUID referenceId);
}
