package zw.miccs.shopos.reporting.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ReportDtos {

	private ReportDtos() {
	}

	public record SalesReportResponse(
		UUID branchId,
		Instant from,
		Instant to,
		int saleCount,
		BigDecimal totalAmount
	) {
	}

	public record CashReconciliationResponse(
		UUID shiftId,
		UUID branchId,
		UUID deviceId,
		String status,
		BigDecimal openingFloat,
		BigDecimal paidIn,
		BigDecimal paidOut,
		BigDecimal drops,
		BigDecimal expectedCash,
		BigDecimal countedCash,
		BigDecimal variance,
		String note,
		Instant openedAt,
		Instant closedAt
	) {
	}

}
