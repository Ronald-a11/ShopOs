package zw.miccs.shopos.sales.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Sale(
	UUID id,
	UUID branchId,
	UUID deviceId,
	UUID clientSaleId,
	String receiptNumber,
	SaleStatus status,
	String currency,
	Instant occurredAt,
	UUID cashierUserId,
	UUID shiftId,
	UUID originalSaleId,
	SaleTotals totals,
	List<SaleItem> items,
	List<SalePayment> payments,
	FiscalStatus fiscalStatus,
	UUID fiscalDocumentId
) {
}
