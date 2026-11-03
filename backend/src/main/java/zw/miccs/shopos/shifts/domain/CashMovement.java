package zw.miccs.shopos.shifts.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CashMovement(
	UUID id,
	UUID shiftId,
	CashMovementType type,
	BigDecimal amount,
	String currency,
	String reason,
	Instant occurredAt
) {
}
