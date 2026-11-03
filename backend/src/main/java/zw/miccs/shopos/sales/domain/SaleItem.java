package zw.miccs.shopos.sales.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItem(
	UUID productId,
	String sku,
	String description,
	BigDecimal quantity,
	BigDecimal unitPrice,
	String taxCode,
	BigDecimal discount,
	BigDecimal lineTotal
) {
}
