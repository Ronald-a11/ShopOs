package zw.miccs.shopos.fiscalisation.application;

import java.time.Instant;
import java.util.UUID;

public record FiscalDayView(
	UUID id,
	String status,
	Instant openedAt,
	Instant closedAt
) {
}
