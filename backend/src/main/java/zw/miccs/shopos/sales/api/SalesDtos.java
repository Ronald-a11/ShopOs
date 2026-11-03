package zw.miccs.shopos.sales.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import zw.miccs.shopos.sales.application.ReturnLineInput;
import zw.miccs.shopos.sales.application.SaleItemInput;
import zw.miccs.shopos.sales.application.SalePaymentInput;
import zw.miccs.shopos.sales.domain.CommitSaleResult;
import zw.miccs.shopos.sales.domain.FiscalStatus;
import zw.miccs.shopos.sales.domain.PaymentMethod;
import zw.miccs.shopos.sales.domain.Sale;
import zw.miccs.shopos.sales.domain.SaleItem;
import zw.miccs.shopos.sales.domain.SalePayment;
import zw.miccs.shopos.sales.domain.SaleStatus;
import zw.miccs.shopos.sales.domain.SaleTotals;

public final class SalesDtos {

	private SalesDtos() {
	}

	public record SaleItemRequest(
		@NotNull UUID productId,
		@NotBlank String quantity,
		String approvedDiscount
	) {
		SaleItemInput toInput() {
			return new SaleItemInput(
				productId,
				new BigDecimal(quantity.trim()),
				approvedDiscount == null || approvedDiscount.isBlank()
					? null
					: new BigDecimal(approvedDiscount.trim()));
		}
	}

	public record SalePaymentRequest(
		@NotNull PaymentMethod method,
		@NotBlank String currency,
		@NotBlank String amount,
		String providerReference,
		String terminalId,
		String cardScheme,
		String maskedPanLast4
	) {
		SalePaymentInput toInput() {
			return new SalePaymentInput(
				method,
				currency,
				new BigDecimal(amount.trim()),
				providerReference,
				terminalId,
				cardScheme,
				maskedPanLast4);
		}
	}

	public record CommitSaleRequest(
		@NotNull UUID clientSaleId,
		@NotNull UUID branchId,
		@NotNull UUID deviceId,
		@NotNull Instant occurredAt,
		@NotNull Integer configVersion,
		@NotBlank String currency,
		@NotEmpty List<@Valid SaleItemRequest> items,
		@NotEmpty List<@Valid SalePaymentRequest> payments
	) {
	}

	public record ReturnLineRequest(
		@NotNull UUID productId,
		@NotBlank String quantity
	) {
		ReturnLineInput toInput() {
			return new ReturnLineInput(productId, new BigDecimal(quantity.trim()));
		}
	}

	public record ReturnSaleRequest(
		@NotNull UUID clientOperationId,
		@NotBlank String reason,
		@NotEmpty List<@Valid ReturnLineRequest> lines
	) {
	}

	public record VoidSaleRequest(
		@NotNull UUID clientOperationId,
		@NotBlank String reason
	) {
	}

	public record SaleTotalsResponse(
		String subtotal,
		String discountTotal,
		String taxTotal,
		String grandTotal
	) {
		static SaleTotalsResponse from(SaleTotals totals) {
			return new SaleTotalsResponse(
				decimal(totals.subtotal()),
				decimal(totals.discountTotal()),
				decimal(totals.taxTotal()),
				decimal(totals.grandTotal()));
		}
	}

	public record SaleItemResponse(
		UUID productId,
		String sku,
		String description,
		String quantity,
		String unitPrice,
		String taxCode,
		String discount,
		String lineTotal
	) {
		static SaleItemResponse from(SaleItem item) {
			return new SaleItemResponse(
				item.productId(),
				item.sku(),
				item.description(),
				decimal(item.quantity()),
				decimal(item.unitPrice()),
				item.taxCode(),
				decimal(item.discount()),
				decimal(item.lineTotal()));
		}
	}

	public record SalePaymentResponse(
		PaymentMethod method,
		String currency,
		String amount,
		String providerReference,
		String terminalId,
		String cardScheme,
		String maskedPanLast4
	) {
		static SalePaymentResponse from(SalePayment payment) {
			return new SalePaymentResponse(
				payment.method(),
				payment.currency(),
				decimal(payment.amount()),
				payment.providerReference(),
				payment.terminalId(),
				payment.cardScheme(),
				payment.maskedPanLast4());
		}
	}

	public record FiscalInfo(FiscalStatus status, UUID documentId) {
	}

	public record InventoryInfo(String status) {
	}

	public record SaleResponse(
		UUID id,
		UUID branchId,
		UUID deviceId,
		UUID clientSaleId,
		String receiptNumber,
		SaleStatus status,
		String currency,
		Instant occurredAt,
		UUID cashierUserId,
		UUID shiftId,
		SaleTotalsResponse totals,
		List<SaleItemResponse> items,
		List<SalePaymentResponse> payments,
		FiscalInfo fiscal
	) {
		static SaleResponse from(Sale sale) {
			return new SaleResponse(
				sale.id(),
				sale.branchId(),
				sale.deviceId(),
				sale.clientSaleId(),
				sale.receiptNumber(),
				sale.status(),
				sale.currency(),
				sale.occurredAt(),
				sale.cashierUserId(),
				sale.shiftId(),
				SaleTotalsResponse.from(sale.totals()),
				sale.items().stream().map(SaleItemResponse::from).toList(),
				sale.payments().stream().map(SalePaymentResponse::from).toList(),
				new FiscalInfo(sale.fiscalStatus(), sale.fiscalDocumentId()));
		}
	}

	public record CommitSaleResponse(
		UUID saleId,
		String receiptNumber,
		boolean replayed,
		SaleTotalsResponse totals,
		String change,
		InventoryInfo inventory,
		FiscalInfo fiscal
	) {
		static CommitSaleResponse from(CommitSaleResult result) {
			Sale sale = result.sale();
			return new CommitSaleResponse(
				sale.id(),
				sale.receiptNumber(),
				result.replayed(),
				SaleTotalsResponse.from(sale.totals()),
				decimal(result.change()),
				new InventoryInfo(result.inventoryStatus()),
				new FiscalInfo(sale.fiscalStatus(), sale.fiscalDocumentId()));
		}
	}

	private static String decimal(BigDecimal value) {
		return value == null ? null : value.stripTrailingZeros().toPlainString();
	}
}
