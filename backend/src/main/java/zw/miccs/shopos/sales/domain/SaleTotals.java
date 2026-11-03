package zw.miccs.shopos.sales.domain;

import java.math.BigDecimal;

public record SaleTotals(
	BigDecimal subtotal,
	BigDecimal discountTotal,
	BigDecimal taxTotal,
	BigDecimal grandTotal
) {
}
