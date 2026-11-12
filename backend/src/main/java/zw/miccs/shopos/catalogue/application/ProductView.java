package zw.miccs.shopos.catalogue.application;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductView(
	UUID id,
	UUID tenantId,
	String sku,
	String name,
	String barcode,
	BigDecimal unitPrice,
	String taxRateCode,
	String status
) {
}
