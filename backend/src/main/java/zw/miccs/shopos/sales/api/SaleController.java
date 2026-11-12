package zw.miccs.shopos.sales.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.sales.api.SaleDtos.CommitSaleRequest;
import zw.miccs.shopos.sales.api.SaleDtos.CommitSaleResponse;
import zw.miccs.shopos.sales.api.SaleDtos.FiscalEffect;
import zw.miccs.shopos.sales.api.SaleDtos.InventoryEffect;
import zw.miccs.shopos.sales.api.SaleDtos.ReturnSaleResponse;
import zw.miccs.shopos.sales.api.SaleDtos.SaleLineResponse;
import zw.miccs.shopos.sales.api.SaleDtos.SalePaymentResponse;
import zw.miccs.shopos.sales.api.SaleDtos.SaleResponse;
import zw.miccs.shopos.sales.api.SaleDtos.SaleSummaryResponse;
import zw.miccs.shopos.sales.api.SaleDtos.VoidSaleResponse;
import zw.miccs.shopos.sales.application.SaleCommitResult;
import zw.miccs.shopos.sales.application.SaleLineCommand;
import zw.miccs.shopos.sales.application.SalePaymentCommand;
import zw.miccs.shopos.sales.application.SaleService;
import zw.miccs.shopos.sales.application.SaleSummaryView;
import zw.miccs.shopos.sales.application.SaleView;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sales")
public class SaleController {

	private final SaleService sales;
	private final CurrentTenantService tenants;

	public SaleController(SaleService sales, CurrentTenantService tenants) {
		this.sales = sales;
		this.tenants = tenants;
	}

	@PostMapping("/sales")
	@Operation(summary = "Commit a sale. Idempotent on tenant, device, and clientSaleId")
	ResponseEntity<CommitSaleResponse> commit(Authentication authentication, @Valid @RequestBody CommitSaleRequest request) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		UUID tenantId = tenants.requirePrimaryTenantId(principal.userId());
		SaleCommitResult result = sales.commit(
			tenantId,
			principal.userId(),
			request.clientSaleId(),
			request.branchId(),
			request.deviceId(),
			request.shiftId(),
			request.currency(),
			request.lines().stream()
				.map(line -> new SaleLineCommand(line.productId(), line.quantity(), line.unitPrice()))
				.toList(),
			request.payments().stream()
				.map(payment -> new SalePaymentCommand(payment.method(), payment.amount(), payment.providerReference()))
				.toList());
		return ResponseEntity.status(201).body(new CommitSaleResponse(
			result.saleId(),
			result.receiptNumber(),
			new InventoryEffect(result.inventoryStatus()),
			new FiscalEffect(result.fiscalStatus(), result.fiscalDocumentId())));
	}

	@GetMapping("/sales")
	@Operation(summary = "List sales, optionally for one branch")
	ResponseEntity<List<SaleSummaryResponse>> list(
		Authentication authentication,
		@RequestParam(required = false) UUID branchId
	) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(sales.list(tenantId, branchId).stream().map(SaleController::toSummary).toList());
	}

	@GetMapping("/sales/{id}")
	@Operation(summary = "Get a sale")
	ResponseEntity<SaleResponse> get(Authentication authentication, @PathVariable UUID id) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(toResponse(sales.get(tenantId, id)));
	}

	@PostMapping("/sales/{id}/returns")
	@Operation(summary = "Return a sale and restore stock")
	ResponseEntity<ReturnSaleResponse> returns(Authentication authentication, @PathVariable UUID id) {
		UUID tenantId = tenantId(authentication);
		SaleView sale = sales.returnSale(tenantId, id);
		return ResponseEntity.ok(new ReturnSaleResponse(sale.id(), sale.status(), new InventoryEffect("APPLIED")));
	}

	@PostMapping("/sales/{id}/void-request")
	@Operation(summary = "Flag a sale as void requested")
	ResponseEntity<VoidSaleResponse> voidRequest(Authentication authentication, @PathVariable UUID id) {
		UUID tenantId = tenantId(authentication);
		SaleSummaryView sale = sales.requestVoid(tenantId, id);
		return ResponseEntity.ok(new VoidSaleResponse(sale.id(), sale.status()));
	}

	private UUID tenantId(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		return tenants.requirePrimaryTenantId(principal.userId());
	}

	private static SaleSummaryResponse toSummary(SaleSummaryView view) {
		return new SaleSummaryResponse(
			view.id(),
			view.branchId(),
			view.deviceId(),
			view.receiptNumber(),
			view.currency(),
			view.status(),
			view.totalAmount(),
			view.createdAt());
	}

	private static SaleResponse toResponse(SaleView view) {
		return new SaleResponse(
			view.id(),
			view.branchId(),
			view.deviceId(),
			view.shiftId(),
			view.clientSaleId(),
			view.receiptNumber(),
			view.currency(),
			view.status(),
			view.totalAmount(),
			view.createdAt(),
			view.lines().stream()
				.map(line -> new SaleLineResponse(line.productId(), line.quantity(), line.unitPrice(), line.lineTotal()))
				.toList(),
			view.payments().stream()
				.map(payment -> new SalePaymentResponse(payment.method(), payment.amount(), payment.providerReference()))
				.toList());
	}

}
