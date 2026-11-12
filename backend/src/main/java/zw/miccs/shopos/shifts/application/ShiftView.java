package zw.miccs.shopos.shifts.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShiftView(
	UUID id,
	UUID tenantId,
	UUID branchId,
	UUID deviceId,
	UUID userId,
	String status,
	BigDecimal openingFloat,
	BigDecimal countedCash,
	BigDecimal expectedCash,
	BigDecimal variance,
	String note,
	Instant openedAt,
	Instant closedAt
) {
}
