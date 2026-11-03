package zw.miccs.shopos.shifts.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashierShift(
	UUID id,
	UUID branchId,
	UUID deviceId,
	UUID cashierUserId,
	ShiftStatus status,
	BigDecimal openingFloat,
	String currency,
	BigDecimal expectedCash,
	BigDecimal countedCash,
	BigDecimal variance,
	Instant openedAt,
	Instant closedAt
) {
}
