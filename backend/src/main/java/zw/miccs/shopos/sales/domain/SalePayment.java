package zw.miccs.shopos.sales.domain;

import java.math.BigDecimal;

public record SalePayment(
	PaymentMethod method,
	String currency,
	BigDecimal amount,
	String providerReference,
	String terminalId,
	String cardScheme,
	String maskedPanLast4
) {
}
