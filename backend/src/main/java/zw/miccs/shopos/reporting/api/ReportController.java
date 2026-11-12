package zw.miccs.shopos.reporting.api;

import java.time.Instant;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.reporting.api.ReportDtos.CashReconciliationResponse;
import zw.miccs.shopos.reporting.api.ReportDtos.SalesReportResponse;
import zw.miccs.shopos.sales.application.SalesQueryService;
import zw.miccs.shopos.sales.application.SalesReportView;
import zw.miccs.shopos.shifts.application.CashReconciliationView;
import zw.miccs.shopos.shifts.application.ShiftQueryService;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reporting")
public class ReportController {

	private final SalesQueryService sales;
	private final ShiftQueryService shifts;
	private final CurrentTenantService tenants;

	public ReportController(SalesQueryService sales, ShiftQueryService shifts, CurrentTenantService tenants) {
		this.sales = sales;
		this.shifts = shifts;
		this.tenants = tenants;
	}

	@GetMapping("/reports/sales")
	@Operation(summary = "Sales totals for a period")
	ResponseEntity<SalesReportResponse> sales(
		Authentication authentication,
		@RequestParam(required = false) UUID branchId,
		@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant from,
		@RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant to
	) {
		UUID tenantId = tenantId(authentication);
		SalesReportView report = sales.salesReport(tenantId, branchId, from, to);
		return ResponseEntity.ok(new SalesReportResponse(
			report.branchId(), report.from(), report.to(), report.saleCount(), report.totalAmount()));
	}

	@GetMapping("/reports/cash-reconciliation")
	@Operation(summary = "Cash reconciliation for a shift")
	ResponseEntity<CashReconciliationResponse> cash(Authentication authentication, @RequestParam UUID shiftId) {
		UUID tenantId = tenantId(authentication);
		CashReconciliationView report = shifts.cashReconciliation(tenantId, shiftId);
		return ResponseEntity.ok(new CashReconciliationResponse(
			report.shiftId(),
			report.branchId(),
			report.deviceId(),
			report.status(),
			report.openingFloat(),
			report.paidIn(),
			report.paidOut(),
			report.drops(),
			report.expectedCash(),
			report.countedCash(),
			report.variance(),
			report.note(),
			report.openedAt(),
			report.closedAt()));
	}

	private UUID tenantId(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		return tenants.requirePrimaryTenantId(principal.userId());
	}

}
