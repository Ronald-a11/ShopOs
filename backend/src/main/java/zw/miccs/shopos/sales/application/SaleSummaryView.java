package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleSummaryView(
	UUID id,
	UUID branchId,
	UUID deviceId,
	String receiptNumber,
	String currency,
	String status,
	BigDecimal totalAmount,
	Instant createdAt
) {
}
