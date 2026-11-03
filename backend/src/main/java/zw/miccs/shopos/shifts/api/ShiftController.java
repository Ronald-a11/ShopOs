package zw.miccs.shopos.shifts.api;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import zw.miccs.shopos.shifts.api.ShiftDtos.CashMovementRequest;
import zw.miccs.shopos.shifts.api.ShiftDtos.CashMovementResponse;
import zw.miccs.shopos.shifts.api.ShiftDtos.CashierShiftResponse;
import zw.miccs.shopos.shifts.api.ShiftDtos.CloseShiftRequest;
import zw.miccs.shopos.shifts.api.ShiftDtos.OpenShiftRequest;
import zw.miccs.shopos.shifts.application.ShiftService;
import zw.miccs.shopos.shifts.domain.CashMovementResult;
import zw.miccs.shopos.shifts.domain.CashierShift;
import zw.miccs.shopos.shifts.domain.OpenShiftResult;

@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {

	private final ShiftService shiftService;

	public ShiftController(ShiftService shiftService) {
		this.shiftService = shiftService;
	}

	@PostMapping("/open")
	ResponseEntity<CashierShiftResponse> openShift(@Valid @RequestBody OpenShiftRequest request) {
		OpenShiftResult result = shiftService.openShift(
			request.branchId(),
			request.deviceId(),
			new BigDecimal(request.openingFloat().trim()),
			request.currency(),
			request.clientOperationId());
		HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(CashierShiftResponse.from(result.shift()));
	}

	@PostMapping("/{id}/cash-movements")
	ResponseEntity<CashMovementResponse> recordCashMovement(
		@PathVariable UUID id,
		@Valid @RequestBody CashMovementRequest request) {
		CashMovementResult result = shiftService.recordCashMovement(
			id,
			request.type(),
			new BigDecimal(request.amount().trim()),
			request.currency(),
			request.reason(),
			request.clientOperationId());
		HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(CashMovementResponse.from(result.movement()));
	}

	@PostMapping("/{id}/close")
	CashierShiftResponse closeShift(
		@PathVariable UUID id,
		@Valid @RequestBody CloseShiftRequest request) {
		CashierShift shift = shiftService.closeShift(
			id,
			request.countedCash().stream().map(ShiftDtos.CountedCashRequest::toInput).toList(),
			request.approvalReason(),
			request.clientOperationId());
		return CashierShiftResponse.from(shift);
	}
}
