package zw.miccs.shopos.shifts.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
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
import zw.miccs.shopos.common.ApiException;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.shifts.api.ShiftDtos.CashMovementRequest;
import zw.miccs.shopos.shifts.api.ShiftDtos.CashMovementResponse;
import zw.miccs.shopos.shifts.api.ShiftDtos.CloseShiftRequest;
import zw.miccs.shopos.shifts.api.ShiftDtos.OpenShiftRequest;
import zw.miccs.shopos.shifts.api.ShiftDtos.ShiftResponse;
import zw.miccs.shopos.shifts.application.CashMovementView;
import zw.miccs.shopos.shifts.application.ShiftService;
import zw.miccs.shopos.shifts.application.ShiftView;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Shifts")
public class ShiftController {

	private final ShiftService shifts;
	private final CurrentTenantService tenants;

	public ShiftController(ShiftService shifts, CurrentTenantService tenants) {
		this.shifts = shifts;
		this.tenants = tenants;
	}

	@PostMapping("/shifts/open")
	@Operation(summary = "Open a cashier shift on a device")
	ResponseEntity<ShiftResponse> open(Authentication authentication, @Valid @RequestBody OpenShiftRequest request) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		UUID tenantId = tenants.requirePrimaryTenantId(principal.userId());
		ShiftView opened = shifts.open(
			tenantId, principal.userId(), request.branchId(), request.deviceId(), request.openingFloat());
		return ResponseEntity.status(201).body(toResponse(opened));
	}

	@GetMapping("/shifts/current")
	@Operation(summary = "Current open shift for a device")
	ResponseEntity<ShiftResponse> current(Authentication authentication, @RequestParam UUID deviceId) {
		UUID tenantId = tenantId(authentication);
		ShiftView shift = shifts.findOpenShiftForDevice(tenantId, deviceId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SHIFT_NOT_OPEN", "Shift not open",
				"No open shift for this device"));
		return ResponseEntity.ok(toResponse(shift));
	}

	@PostMapping("/shifts/{id}/cash-movements")
	@Operation(summary = "Record a paid-in, paid-out, or drop")
	ResponseEntity<CashMovementResponse> cashMovement(
		Authentication authentication,
		@PathVariable UUID id,
		@Valid @RequestBody CashMovementRequest request
	) {
		UUID tenantId = tenantId(authentication);
		CashMovementView created = shifts.addCashMovement(tenantId, id, request.type(), request.amount(), request.note());
		return ResponseEntity.status(201).body(toResponse(created));
	}

	@PostMapping("/shifts/{id}/close")
	@Operation(summary = "Close a shift with counted cash")
	ResponseEntity<ShiftResponse> close(
		Authentication authentication,
		@PathVariable UUID id,
		@Valid @RequestBody CloseShiftRequest request
	) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(toResponse(shifts.close(tenantId, id, request.countedCash(), request.note())));
	}

	private UUID tenantId(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		return tenants.requirePrimaryTenantId(principal.userId());
	}

	private static ShiftResponse toResponse(ShiftView view) {
		return new ShiftResponse(
			view.id(),
			view.branchId(),
			view.deviceId(),
			view.userId(),
			view.status(),
			view.openingFloat(),
			view.countedCash(),
			view.expectedCash(),
			view.variance(),
			view.note(),
			view.openedAt(),
			view.closedAt());
	}

	private static CashMovementResponse toResponse(CashMovementView view) {
		return new CashMovementResponse(view.id(), view.shiftId(), view.type(), view.amount(), view.note(), view.createdAt());
	}

}
