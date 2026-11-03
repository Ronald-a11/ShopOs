package zw.miccs.shopos.sales.api;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.sales.api.SalesDtos.CommitSaleRequest;
import zw.miccs.shopos.sales.api.SalesDtos.CommitSaleResponse;
import zw.miccs.shopos.sales.api.SalesDtos.ReturnSaleRequest;
import zw.miccs.shopos.sales.api.SalesDtos.SaleResponse;
import zw.miccs.shopos.sales.api.SalesDtos.VoidSaleRequest;
import zw.miccs.shopos.sales.application.SalesService;
import zw.miccs.shopos.sales.domain.CommitSaleResult;
import zw.miccs.shopos.sales.domain.Sale;

@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

	private final SalesService salesService;

	public SalesController(SalesService salesService) {
		this.salesService = salesService;
	}

	@PostMapping
	ResponseEntity<CommitSaleResponse> commitSale(@Valid @RequestBody CommitSaleRequest request) {
		CommitSaleResult result = salesService.commitSale(
			request.clientSaleId(),
			request.branchId(),
			request.deviceId(),
			request.occurredAt(),
			request.configVersion(),
			request.currency(),
			request.items().stream().map(SalesDtos.SaleItemRequest::toInput).toList(),
			request.payments().stream().map(SalesDtos.SalePaymentRequest::toInput).toList());
		HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(CommitSaleResponse.from(result));
	}

	@GetMapping
	PageResponse<SaleResponse> search(
		@RequestParam(required = false) UUID branchId,
		@RequestParam(required = false) Instant from,
		@RequestParam(required = false) Instant to,
		@RequestParam(required = false) UUID cashierUserId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort) {
		PageResponse<Sale> result = salesService.search(
			branchId, from, to, cashierUserId, toPageable(page, size, sort));
		return new PageResponse<>(
			result.content().stream().map(SaleResponse::from).toList(),
			result.page());
	}

	@GetMapping("/{id}")
	SaleResponse get(@PathVariable UUID id) {
		return SaleResponse.from(salesService.get(id));
	}

	@PostMapping("/{id}/returns")
	@ResponseStatus(HttpStatus.CREATED)
	SaleResponse initiateReturn(@PathVariable UUID id, @Valid @RequestBody ReturnSaleRequest request) {
		return SaleResponse.from(salesService.initiateReturn(
			id,
			request.clientOperationId(),
			request.reason(),
			request.lines().stream().map(SalesDtos.ReturnLineRequest::toInput).toList()));
	}

	@PostMapping("/{id}/void-request")
	@ResponseStatus(HttpStatus.CREATED)
	SaleResponse requestVoid(@PathVariable UUID id, @Valid @RequestBody VoidSaleRequest request) {
		return SaleResponse.from(salesService.requestVoid(id, request.clientOperationId(), request.reason()));
	}

	private static Pageable toPageable(int page, int size, String sort) {
		int safeSize = Math.min(Math.max(size, 1), 200);
		int safePage = Math.max(page, 0);
		if (sort == null || sort.isBlank()) {
			return PageRequest.of(safePage, safeSize, Sort.by("occurredAt").descending());
		}
		String[] parts = sort.split(",");
		String property = parts[0].trim();
		Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
			? Sort.Direction.DESC
			: Sort.Direction.ASC;
		return PageRequest.of(safePage, safeSize, Sort.by(direction, property));
	}
}
