package zw.miccs.shopos.sales.domain;

import java.math.BigDecimal;

public record CommitSaleResult(
	Sale sale,
	boolean replayed,
	BigDecimal change,
	String inventoryStatus
) {
}
