package zw.miccs.shopos.shifts.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class ShiftDtos {

	private ShiftDtos() {
	}

	public record OpenShiftRequest(
		@NotNull UUID branchId,
		@NotNull UUID deviceId,
		@NotNull @PositiveOrZero BigDecimal openingFloat
	) {
	}

	public record CashMovementRequest(
		@NotBlank String type,
		@NotNull @Positive BigDecimal amount,
		@Size(max = 500) String note
	) {
	}

	public record CloseShiftRequest(
		@NotNull @PositiveOrZero BigDecimal countedCash,
		@Size(max = 500) String note
	) {
	}

	public record ShiftResponse(
		UUID id,
		UUID branchId,
		UUID deviceId,
		UUID userId,
		String status,
		BigDecimal openingFloat,
		BigDecimal countedCash,
		BigDecimal expectedCash,
		BigDecimal variance,
		String note,
		Instant openedAt,
		Instant closedAt
	) {
	}

	public record CashMovementResponse(
		UUID id,
		UUID shiftId,
		String type,
		BigDecimal amount,
		String note,
		Instant createdAt
	) {
	}

}
