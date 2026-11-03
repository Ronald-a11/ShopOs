package zw.miccs.shopos.catalogue.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record Product(
	UUID id,
	UUID tenantId,
	String sku,
	String name,
	String description,
	String taxCode,
	BigDecimal unitPrice,
	String currency,
	boolean active,
	List<ProductBarcode> barcodes
) {
}
