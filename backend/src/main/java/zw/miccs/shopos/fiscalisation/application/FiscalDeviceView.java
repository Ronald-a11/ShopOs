package zw.miccs.shopos.fiscalisation.application;

import java.time.Instant;
import java.util.UUID;

public record FiscalDeviceView(
	UUID id,
	String code,
	String name,
	String status,
	Instant createdAt
) {
}
