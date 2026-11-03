package zw.miccs.shopos.catalogue.domain;

import java.math.BigDecimal;

public record ProductBarcode(
	String barcode,
	BigDecimal unitMultiplier
) {
}
