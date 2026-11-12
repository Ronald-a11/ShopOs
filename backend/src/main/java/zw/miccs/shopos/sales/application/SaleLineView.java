package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleLineView(
	UUID productId,
	BigDecimal quantity,
	BigDecimal unitPrice,
	BigDecimal lineTotal
) {
}
