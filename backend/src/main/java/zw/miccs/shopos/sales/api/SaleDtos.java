package zw.miccs.shopos.sales.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class SaleDtos {

	private SaleDtos() {
	}

	public record SaleLineRequest(
		@NotNull UUID productId,
		@NotNull @Positive BigDecimal quantity,
		@NotNull @PositiveOrZero BigDecimal unitPrice
	) {
	}

	public record SalePaymentRequest(
		@NotBlank @Size(max = 32) String method,
		@NotNull @Positive BigDecimal amount,
		@Size(max = 128) String providerReference
	) {
	}

	public record CommitSaleRequest(
		@NotBlank @Size(max = 64) String clientSaleId,
		@NotNull UUID branchId,
		@NotNull UUID deviceId,
		@NotNull UUID shiftId,
		@NotBlank @Size(min = 3, max = 8) String currency,
		@NotEmpty List<@Valid SaleLineRequest> lines,
		@NotEmpty List<@Valid SalePaymentRequest> payments
	) {
	}

	public record InventoryEffect(String status) {
	}

	public record FiscalEffect(String status, UUID documentId) {
	}

	public record CommitSaleResponse(
		UUID saleId,
		String receiptNumber,
		InventoryEffect inventory,
		FiscalEffect fiscal
	) {
	}

	public record SaleSummaryResponse(
		UUID id,
		UUID branchId,
		UUID deviceId,
		String receiptNumber,
		String currency,
		String status,
		BigDecimal totalAmount,
		Instant createdAt
	) {
	}

	public record SaleLineResponse(
		UUID productId,
		BigDecimal quantity,
		BigDecimal unitPrice,
		BigDecimal lineTotal
	) {
	}

	public record SalePaymentResponse(
		String method,
		BigDecimal amount,
		String providerReference
	) {
	}

	public record SaleResponse(
		UUID id,
		UUID branchId,
		UUID deviceId,
		UUID shiftId,
		String clientSaleId,
		String receiptNumber,
		String currency,
		String status,
		BigDecimal totalAmount,
		Instant createdAt,
		List<SaleLineResponse> lines,
		List<SalePaymentResponse> payments
	) {
	}

	public record ReturnSaleResponse(
		UUID saleId,
		String status,
		InventoryEffect inventory
	) {
	}

	public record VoidSaleResponse(
		UUID saleId,
		String status
	) {
	}

}
