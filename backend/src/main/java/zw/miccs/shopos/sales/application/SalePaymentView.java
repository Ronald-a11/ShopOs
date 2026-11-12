package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;

public record SalePaymentView(
	String method,
	BigDecimal amount,
	String providerReference
) {
}
