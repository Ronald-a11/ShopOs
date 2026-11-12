package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SalesReportView(
	UUID branchId,
	Instant from,
	Instant to,
	int saleCount,
	BigDecimal totalAmount
) {
}
