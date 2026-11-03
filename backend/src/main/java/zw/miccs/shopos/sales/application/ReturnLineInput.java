package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.util.UUID;

public record ReturnLineInput(UUID productId, BigDecimal quantity) {
}
