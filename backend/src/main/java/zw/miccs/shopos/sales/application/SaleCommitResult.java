package zw.miccs.shopos.sales.application;

import java.util.UUID;

public record SaleCommitResult(
	UUID saleId,
	String receiptNumber,
	String inventoryStatus,
	String fiscalStatus,
	UUID fiscalDocumentId
) {
}
