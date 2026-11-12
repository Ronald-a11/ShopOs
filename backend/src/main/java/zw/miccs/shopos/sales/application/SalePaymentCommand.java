package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;

public record SalePaymentCommand(
	String method,
	BigDecimal amount,
	String providerReference
) {
}
