package zw.miccs.shopos.shifts.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashReconciliationView(
	UUID shiftId,
	UUID branchId,
	UUID deviceId,
	String status,
	BigDecimal openingFloat,
	BigDecimal paidIn,
	BigDecimal paidOut,
	BigDecimal drops,
	BigDecimal expectedCash,
	BigDecimal countedCash,
	BigDecimal variance,
	String note,
	Instant openedAt,
	Instant closedAt
) {
}
