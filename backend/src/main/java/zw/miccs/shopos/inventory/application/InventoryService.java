package zw.miccs.shopos.inventory.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Stock ledger. Sales calls {@link #applySale} and {@link #applyReturn}; it does not touch balances itself.
 */
public interface InventoryService {

	List<BalanceView> listBalances(UUID tenantId, UUID branchId);

	List<BalanceView> receive(UUID tenantId, UUID branchId, List<StockLine> lines, String note);

	BalanceView adjust(UUID tenantId, UUID branchId, UUID productId, BigDecimal quantityDelta, String reason);

	List<MovementView> listMovements(UUID tenantId, UUID branchId);

	List<BalanceView> listLowStock(UUID tenantId, UUID branchId, BigDecimal threshold);

	void applySale(UUID tenantId, UUID branchId, UUID saleId, List<StockLine> lines);

	void applyReturn(UUID tenantId, UUID branchId, UUID saleId, List<StockLine> lines);

}
