package zw.miccs.shopos.fiscalisation.application;

import java.time.Instant;
import java.util.UUID;

public record FiscalDocumentView(
	UUID documentId,
	UUID saleId,
	String status,
	int attempts,
	String lastError,
	Instant createdAt,
	Instant updatedAt
) {
}
