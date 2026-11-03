package zw.miccs.shopos.inventory.application;

import java.math.BigDecimal;
import java.util.UUID;

public record ReceiptLineInput(UUID productId, BigDecimal quantity) {
}
