package zw.miccs.shopos.catalogue.application;

import java.math.BigDecimal;

public record BarcodeInput(String barcode, BigDecimal unitMultiplier) {
}
