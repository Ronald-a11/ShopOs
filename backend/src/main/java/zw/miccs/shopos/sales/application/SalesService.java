package zw.miccs.shopos.sales.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.sales.domain.CommitSaleResult;
import zw.miccs.shopos.sales.domain.Sale;

public interface SalesService {

	CommitSaleResult commitSale(
		UUID clientSaleId,
		UUID branchId,
		UUID deviceId,
		Instant occurredAt,
		int configVersion,
		String currency,
		List<SaleItemInput> items,
		List<SalePaymentInput> payments);

	PageResponse<Sale> search(
		UUID branchId,
		Instant from,
		Instant to,
		UUID cashierUserId,
		Pageable pageable);

	Sale get(UUID saleId);

	Sale initiateReturn(UUID saleId, UUID clientOperationId, String reason, List<ReturnLineInput> lines);

	Sale requestVoid(UUID saleId, UUID clientOperationId, String reason);
}
