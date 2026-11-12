package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SaleView(
	UUID id,
	UUID branchId,
	UUID deviceId,
	UUID shiftId,
	String clientSaleId,
	String receiptNumber,
	String currency,
	String status,
	BigDecimal totalAmount,
	Instant createdAt,
	List<SaleLineView> lines,
	List<SalePaymentView> payments
) {
}
