package zw.miccs.shopos.shifts.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashMovementView(
	UUID id,
	UUID shiftId,
	String type,
	BigDecimal amount,
	String note,
	Instant createdAt
) {
}
