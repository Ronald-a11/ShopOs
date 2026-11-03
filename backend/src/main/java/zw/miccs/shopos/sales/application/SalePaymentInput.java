package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;

import zw.miccs.shopos.sales.domain.PaymentMethod;

public record SalePaymentInput(
	PaymentMethod method,
	String currency,
	BigDecimal amount,
	String providerReference,
	String terminalId,
	String cardScheme,
	String maskedPanLast4
) {
}
