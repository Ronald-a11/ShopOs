package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleItemInput(UUID productId, BigDecimal quantity, BigDecimal approvedDiscount) {
}
