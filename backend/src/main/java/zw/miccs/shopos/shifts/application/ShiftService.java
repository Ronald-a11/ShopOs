package zw.miccs.shopos.shifts.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import zw.miccs.shopos.shifts.domain.CashMovementResult;
import zw.miccs.shopos.shifts.domain.CashMovementType;
import zw.miccs.shopos.shifts.domain.CashierShift;
import zw.miccs.shopos.shifts.domain.OpenShiftResult;

public interface ShiftService {

	OpenShiftResult openShift(
		UUID branchId,
		UUID deviceId,
		BigDecimal openingFloat,
		String currency,
		UUID clientOperationId);

	CashMovementResult recordCashMovement(
		UUID shiftId,
		CashMovementType type,
		BigDecimal amount,
		String currency,
		String reason,
		UUID clientOperationId);

	CashierShift closeShift(
		UUID shiftId,
		List<CountedCashInput> countedCash,
		String approvalReason,
		UUID clientOperationId);

	/**
	 * Resolve the current user's open shift for the till. Used by sales next.
	 */
	CashierShift requireOpenShift(UUID branchId, UUID deviceId);

	/**
	 * Adjust expected cash for sale CASH tenders (positive) or refunds/voids (negative).
	 */
	void applyCashTenderDelta(UUID shiftId, BigDecimal delta);
}
