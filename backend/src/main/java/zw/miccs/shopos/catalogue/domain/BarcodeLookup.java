package zw.miccs.shopos.catalogue.domain;

import java.math.BigDecimal;

public record BarcodeLookup(
	Product product,
	BigDecimal unitMultiplier,
	String barcode
) {
}
