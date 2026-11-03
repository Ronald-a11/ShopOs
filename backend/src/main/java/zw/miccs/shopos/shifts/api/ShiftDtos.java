package zw.miccs.shopos.shifts.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import zw.miccs.shopos.shifts.application.CountedCashInput;
import zw.miccs.shopos.shifts.domain.CashMovement;
import zw.miccs.shopos.shifts.domain.CashMovementType;
import zw.miccs.shopos.shifts.domain.CashierShift;
import zw.miccs.shopos.shifts.domain.ShiftStatus;

public final class ShiftDtos {

	private ShiftDtos() {
	}

	public record OpenShiftRequest(
		@NotNull UUID branchId,
		@NotNull UUID deviceId,
		@NotBlank String openingFloat,
		@NotBlank String currency,
		UUID clientOperationId
	) {
	}

	public record CashMovementRequest(
		@NotNull CashMovementType type,
		@NotBlank String amount,
		@NotBlank String currency,
		String reason,
		UUID clientOperationId
	) {
	}

	public record CountedCashRequest(
		@NotBlank String currency,
		@NotBlank String amount
	) {
		CountedCashInput toInput() {
			return new CountedCashInput(currency, new BigDecimal(amount.trim()));
		}
	}

	public record CloseShiftRequest(
		@NotEmpty List<@Valid CountedCashRequest> countedCash,
		String approvalReason,
		UUID clientOperationId
	) {
	}

	public record CashierShiftResponse(
		UUID id,
		UUID branchId,
		UUID deviceId,
		UUID cashierUserId,
		ShiftStatus status,
		String openingFloat,
		String currency,
		String expectedCash,
		String countedCash,
		String variance,
		Instant openedAt,
		Instant closedAt
	) {
		static CashierShiftResponse from(CashierShift shift) {
			return new CashierShiftResponse(
				shift.id(),
				shift.branchId(),
				shift.deviceId(),
				shift.cashierUserId(),
				shift.status(),
				decimal(shift.openingFloat()),
				shift.currency(),
				decimal(shift.expectedCash()),
				decimal(shift.countedCash()),
				decimal(shift.variance()),
				shift.openedAt(),
				shift.closedAt());
		}
	}

	public record CashMovementResponse(
		UUID id,
		UUID shiftId,
		CashMovementType type,
		String amount,
		String currency,
		String reason,
		Instant occurredAt
	) {
		static CashMovementResponse from(CashMovement movement) {
			return new CashMovementResponse(
				movement.id(),
				movement.shiftId(),
				movement.type(),
				decimal(movement.amount()),
				movement.currency(),
				movement.reason(),
				movement.occurredAt());
		}
	}

	private static String decimal(BigDecimal value) {
		return value == null ? null : value.stripTrailingZeros().toPlainString();
	}
}
