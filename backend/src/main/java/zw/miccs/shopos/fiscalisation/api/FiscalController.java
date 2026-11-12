package zw.miccs.shopos.fiscalisation.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.fiscalisation.api.FiscalDtos.FiscalDayResponse;
import zw.miccs.shopos.fiscalisation.api.FiscalDtos.FiscalDeviceResponse;
import zw.miccs.shopos.fiscalisation.api.FiscalDtos.FiscalDocumentResponse;
import zw.miccs.shopos.fiscalisation.api.FiscalDtos.FiscalHealthResponse;
import zw.miccs.shopos.fiscalisation.api.FiscalDtos.RegisterFiscalDeviceRequest;
import zw.miccs.shopos.fiscalisation.application.FiscalDayView;
import zw.miccs.shopos.fiscalisation.application.FiscalDeviceView;
import zw.miccs.shopos.fiscalisation.application.FiscalDocumentView;
import zw.miccs.shopos.fiscalisation.application.FiscalHealthView;
import zw.miccs.shopos.fiscalisation.application.FiscalService;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Fiscalisation")
public class FiscalController {

	private final FiscalService fiscal;
	private final CurrentTenantService tenants;

	public FiscalController(FiscalService fiscal, CurrentTenantService tenants) {
		this.fiscal = fiscal;
		this.tenants = tenants;
	}

	@GetMapping("/fiscal/devices")
	@Operation(summary = "List fiscal devices")
	ResponseEntity<List<FiscalDeviceResponse>> listDevices(Authentication authentication) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(fiscal.listDevices(tenantId).stream().map(FiscalController::toResponse).toList());
	}

	@PostMapping("/fiscal/devices")
	@Operation(summary = "Register a fiscal device")
	ResponseEntity<FiscalDeviceResponse> registerDevice(
		Authentication authentication,
		@Valid @RequestBody RegisterFiscalDeviceRequest request
	) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.status(201).body(toResponse(fiscal.registerDevice(tenantId, request.code(), request.name())));
	}

	@GetMapping("/fiscal/documents/{id}")
	@Operation(summary = "Get a fiscal document")
	ResponseEntity<FiscalDocumentResponse> getDocument(Authentication authentication, @PathVariable UUID id) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(toResponse(fiscal.getDocument(tenantId, id)));
	}

	@PostMapping("/fiscal/documents/{id}/retry")
	@Operation(summary = "Queue a fiscal document for submission again")
	ResponseEntity<FiscalDocumentResponse> retry(Authentication authentication, @PathVariable UUID id) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(toResponse(fiscal.retry(tenantId, id)));
	}

	@GetMapping("/fiscal/health")
	@Operation(summary = "Fiscal adapter health")
	ResponseEntity<FiscalHealthResponse> health(Authentication authentication) {
		tenantId(authentication);
		FiscalHealthView health = fiscal.health();
		return ResponseEntity.ok(new FiscalHealthResponse(health.status(), health.adapter()));
	}

	@PostMapping("/fiscal/days/open")
	@Operation(summary = "Open the fiscal day")
	ResponseEntity<FiscalDayResponse> openDay(Authentication authentication) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.status(201).body(toResponse(fiscal.openDay(tenantId)));
	}

	@PostMapping("/fiscal/days/close")
	@Operation(summary = "Close the open fiscal day")
	ResponseEntity<FiscalDayResponse> closeDay(Authentication authentication) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(toResponse(fiscal.closeDay(tenantId)));
	}

	private UUID tenantId(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		return tenants.requirePrimaryTenantId(principal.userId());
	}

	private static FiscalDeviceResponse toResponse(FiscalDeviceView view) {
		return new FiscalDeviceResponse(view.id(), view.code(), view.name(), view.status(), view.createdAt());
	}

	private static FiscalDocumentResponse toResponse(FiscalDocumentView view) {
		return new FiscalDocumentResponse(
			view.documentId(),
			view.saleId(),
			view.status(),
			view.attempts(),
			view.lastError(),
			view.createdAt(),
			view.updatedAt());
	}

	private static FiscalDayResponse toResponse(FiscalDayView view) {
		return new FiscalDayResponse(view.id(), view.status(), view.openedAt(), view.closedAt());
	}

}
