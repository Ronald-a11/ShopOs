package zw.miccs.shopos.shifts.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.application.BranchService;
import zw.miccs.shopos.common.api.ConflictException;
import zw.miccs.shopos.common.api.ForbiddenException;
import zw.miccs.shopos.common.api.NotFoundException;
import zw.miccs.shopos.common.api.UnprocessableEntityException;
import zw.miccs.shopos.shifts.domain.CashMovement;
import zw.miccs.shopos.shifts.domain.CashMovementResult;
import zw.miccs.shopos.shifts.domain.CashMovementType;
import zw.miccs.shopos.shifts.domain.CashierShift;
import zw.miccs.shopos.shifts.domain.OpenShiftResult;
import zw.miccs.shopos.shifts.domain.ShiftStatus;
import zw.miccs.shopos.shifts.infrastructure.CashierShiftEntity;
import zw.miccs.shopos.shifts.infrastructure.CashierShiftJpaRepository;
import zw.miccs.shopos.shifts.infrastructure.ShiftCashMovementEntity;
import zw.miccs.shopos.shifts.infrastructure.ShiftCashMovementJpaRepository;
import zw.miccs.shopos.shifts.infrastructure.ShiftOperationEntity;
import zw.miccs.shopos.shifts.infrastructure.ShiftOperationJpaRepository;
import zw.miccs.shopos.tenancy.application.TenantAccess;
import zw.miccs.shopos.tenancy.application.TenantContext;
import zw.miccs.shopos.tenancy.domain.Role;

@Service
@Transactional
public class ShiftServiceImpl implements ShiftService {

	private final CashierShiftJpaRepository shifts;
	private final ShiftCashMovementJpaRepository movements;
	private final ShiftOperationJpaRepository operations;
	private final BranchService branchService;
	private final TenantAccess tenantAccess;

	public ShiftServiceImpl(
		CashierShiftJpaRepository shifts,
		ShiftCashMovementJpaRepository movements,
		ShiftOperationJpaRepository operations,
		BranchService branchService,
		TenantAccess tenantAccess) {
		this.shifts = shifts;
		this.movements = movements;
		this.operations = operations;
		this.branchService = branchService;
		this.tenantAccess = tenantAccess;
	}

	@Override
	public OpenShiftResult openShift(
		UUID branchId,
		UUID deviceId,
		BigDecimal openingFloat,
		String currency,
		UUID clientOperationId) {
		requireOperateRole();
		UUID tenantId = tenantAccess.requireTenantId();
		UUID cashierUserId = TenantContext.require().userId();

		if (clientOperationId != null) {
			var existing = operations.findByTenantIdAndClientOperationId(tenantId, clientOperationId);
			if (existing.isPresent()) {
				CashierShiftEntity shift = requireShift(tenantId, existing.get().getAggregateId());
				return new OpenShiftResult(toDomain(shift), true);
			}
		}

		branchService.get(branchId);
		branchService.getDevice(branchId, deviceId);

		if (openingFloat == null || openingFloat.compareTo(BigDecimal.ZERO) < 0) {
			throw new UnprocessableEntityException("INVALID_OPENING_FLOAT", "openingFloat must be zero or greater");
		}
		String normalizedCurrency = requireCurrency(currency);

		if (shifts.findByTenantIdAndCashierUserIdAndDeviceIdAndStatus(
			tenantId, cashierUserId, deviceId, ShiftStatus.OPEN).isPresent()) {
			throw new ConflictException("SHIFT_ALREADY_OPEN", "An open shift already exists for this cashier and device");
		}

		CashierShiftEntity entity = new CashierShiftEntity();
		entity.setTenantId(tenantId);
		entity.setBranchId(branchId);
		entity.setDeviceId(deviceId);
		entity.setCashierUserId(cashierUserId);
		entity.setStatus(ShiftStatus.OPEN);
		entity.setOpeningFloat(openingFloat);
		entity.setCurrency(normalizedCurrency);
		entity.setExpectedCash(openingFloat);

		try {
			CashierShiftEntity saved = shifts.saveAndFlush(entity);
			saveOperation(tenantId, clientOperationId, ShiftOperationEntity.TYPE_OPEN, saved.getId());
			return new OpenShiftResult(toDomain(saved), false);
		}
		catch (DataIntegrityViolationException ex) {
			throw new ConflictException("SHIFT_ALREADY_OPEN", "An open shift already exists for this cashier and device");
		}
	}

	@Override
	public CashMovementResult recordCashMovement(
		UUID shiftId,
		CashMovementType type,
		BigDecimal amount,
		String currency,
		String reason,
		UUID clientOperationId) {
		requireOperateRole();
		UUID tenantId = tenantAccess.requireTenantId();

		if (clientOperationId != null) {
			var existing = operations.findByTenantIdAndClientOperationId(tenantId, clientOperationId);
			if (existing.isPresent()) {
				ShiftCashMovementEntity movement = movements
					.findByTenantIdAndId(tenantId, existing.get().getAggregateId())
					.orElseThrow(() -> new NotFoundException("CASH_MOVEMENT_NOT_FOUND", "Cash movement not found"));
				return new CashMovementResult(toDomain(movement), true);
			}
		}

		if (type == null) {
			throw new UnprocessableEntityException("INVALID_MOVEMENT_TYPE", "Cash movement type is required");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new UnprocessableEntityException("INVALID_AMOUNT", "Cash movement amount must be greater than zero");
		}

		CashierShiftEntity shift = shifts.findForUpdate(tenantId, shiftId)
			.orElseThrow(() -> new NotFoundException("SHIFT_NOT_FOUND", "Shift not found"));
		requireShiftAccess(shift);
		if (shift.getStatus() != ShiftStatus.OPEN) {
			throw new ConflictException("SHIFT_NOT_OPEN", "Cash movements are only allowed on an open shift");
		}

		String normalizedCurrency = requireCurrency(currency);
		if (!shift.getCurrency().equals(normalizedCurrency)) {
			throw new UnprocessableEntityException("CURRENCY_MISMATCH", "Cash movement currency must match the shift currency");
		}

		ShiftCashMovementEntity movement = new ShiftCashMovementEntity();
		movement.setTenantId(tenantId);
		movement.setShiftId(shift.getId());
		movement.setType(type);
		movement.setAmount(amount);
		movement.setCurrency(normalizedCurrency);
		movement.setReason(blankToNull(reason));
		ShiftCashMovementEntity saved = movements.save(movement);

		BigDecimal nextExpected = switch (type) {
			case PAID_IN -> shift.getExpectedCash().add(amount);
			case PAID_OUT, DROP -> shift.getExpectedCash().subtract(amount);
		};
		shift.setExpectedCash(nextExpected);
		shifts.save(shift);
		saveOperation(tenantId, clientOperationId, ShiftOperationEntity.TYPE_CASH_MOVEMENT, saved.getId());
		return new CashMovementResult(toDomain(saved), false);
	}

	@Override
	public CashierShift closeShift(
		UUID shiftId,
		List<CountedCashInput> countedCash,
		String approvalReason,
		UUID clientOperationId) {
		requireOperateRole();
		UUID tenantId = tenantAccess.requireTenantId();

		if (clientOperationId != null) {
			var existing = operations.findByTenantIdAndClientOperationId(tenantId, clientOperationId);
			if (existing.isPresent()) {
				return toDomain(requireShift(tenantId, existing.get().getAggregateId()));
			}
		}

		if (countedCash == null || countedCash.isEmpty()) {
			throw new UnprocessableEntityException("COUNTED_CASH_REQUIRED", "At least one counted cash entry is required");
		}

		CashierShiftEntity shift = shifts.findForUpdate(tenantId, shiftId)
			.orElseThrow(() -> new NotFoundException("SHIFT_NOT_FOUND", "Shift not found"));
		requireShiftAccess(shift);
		if (shift.getStatus() != ShiftStatus.OPEN) {
			throw new ConflictException("SHIFT_NOT_OPEN", "Shift is already closed");
		}

		BigDecimal counted = resolveCountedAmount(shift.getCurrency(), countedCash);
		BigDecimal expected = shift.getExpectedCash();
		BigDecimal variance = counted.subtract(expected);

		Role role = TenantContext.require().role();
		if (variance.compareTo(BigDecimal.ZERO) != 0 && !role.canApproveShiftVariance()) {
			throw new ConflictException(
				"SHIFT_VARIANCE_APPROVAL_REQUIRED",
				"Non-zero variance requires supervisor approval");
		}

		shift.setExpectedCash(expected);
		shift.setCountedCash(counted);
		shift.setVariance(variance);
		shift.setApprovalReason(blankToNull(approvalReason));
		shift.setStatus(ShiftStatus.CLOSED);
		shift.setClosedAt(Instant.now());
		CashierShiftEntity saved = shifts.save(shift);
		saveOperation(tenantId, clientOperationId, ShiftOperationEntity.TYPE_CLOSE, saved.getId());
		return toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public CashierShift requireOpenShift(UUID branchId, UUID deviceId) {
		requireOperateRole();
		branchService.get(branchId);
		branchService.getDevice(branchId, deviceId);
		UUID tenantId = tenantAccess.requireTenantId();
		UUID cashierUserId = TenantContext.require().userId();
		return shifts
			.findByTenantIdAndCashierUserIdAndDeviceIdAndStatus(
				tenantId, cashierUserId, deviceId, ShiftStatus.OPEN)
			.map(this::toDomain)
			.orElseThrow(() -> new NotFoundException("SHIFT_NOT_FOUND", "No open shift for this cashier and device"));
	}

	@Override
	public void applyCashTenderDelta(UUID shiftId, BigDecimal delta) {
		if (delta == null || delta.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		UUID tenantId = tenantAccess.requireTenantId();
		CashierShiftEntity shift = shifts.findForUpdate(tenantId, shiftId)
			.orElseThrow(() -> new NotFoundException("SHIFT_NOT_FOUND", "Shift not found"));
		if (shift.getStatus() != ShiftStatus.OPEN) {
			throw new ConflictException("SHIFT_NOT_OPEN", "Cash tender adjustments require an open shift");
		}
		shift.setExpectedCash(shift.getExpectedCash().add(delta));
		shifts.save(shift);
	}

	private static BigDecimal resolveCountedAmount(String shiftCurrency, List<CountedCashInput> countedCash) {
		BigDecimal total = null;
		for (CountedCashInput entry : countedCash) {
			if (entry.amount() == null || entry.amount().compareTo(BigDecimal.ZERO) < 0) {
				throw new UnprocessableEntityException("INVALID_COUNTED_CASH", "Counted cash amount must be zero or greater");
			}
			String currency = requireCurrency(entry.currency());
			if (!shiftCurrency.equals(currency)) {
				throw new UnprocessableEntityException("CURRENCY_MISMATCH", "Counted cash currency must match the shift currency");
			}
			total = total == null ? entry.amount() : total.add(entry.amount());
		}
		return total;
	}

	private void saveOperation(UUID tenantId, UUID clientOperationId, String operationType, UUID aggregateId) {
		if (clientOperationId == null) {
			return;
		}
		ShiftOperationEntity op = new ShiftOperationEntity();
		op.setTenantId(tenantId);
		op.setClientOperationId(clientOperationId);
		op.setOperationType(operationType);
		op.setAggregateId(aggregateId);
		operations.save(op);
	}

	private CashierShiftEntity requireShift(UUID tenantId, UUID shiftId) {
		return shifts.findByTenantIdAndId(tenantId, shiftId)
			.orElseThrow(() -> new NotFoundException("SHIFT_NOT_FOUND", "Shift not found"));
	}

	private void requireShiftAccess(CashierShiftEntity shift) {
		tenantAccess.requireBranch(shift.getBranchId());
		UUID userId = TenantContext.require().userId();
		Role role = TenantContext.require().role();
		if (!userId.equals(shift.getCashierUserId()) && !role.canApproveShiftVariance()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to access this shift");
		}
	}

	private static void requireOperateRole() {
		if (!TenantContext.require().role().canOperateShift()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to operate shifts");
		}
	}

	private static String requireCurrency(String currency) {
		if (currency == null || currency.isBlank() || currency.trim().length() != 3) {
			throw new UnprocessableEntityException("INVALID_CURRENCY", "currency must be a 3-letter ISO code");
		}
		return currency.trim().toUpperCase(Locale.ROOT);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private CashierShift toDomain(CashierShiftEntity entity) {
		return new CashierShift(
			entity.getId(),
			entity.getBranchId(),
			entity.getDeviceId(),
			entity.getCashierUserId(),
			entity.getStatus(),
			entity.getOpeningFloat(),
			entity.getCurrency(),
			entity.getExpectedCash(),
			entity.getCountedCash(),
			entity.getVariance(),
			entity.getOpenedAt(),
			entity.getClosedAt());
	}

	private CashMovement toDomain(ShiftCashMovementEntity entity) {
		return new CashMovement(
			entity.getId(),
			entity.getShiftId(),
			entity.getType(),
			entity.getAmount(),
			entity.getCurrency(),
			entity.getReason(),
			entity.getOccurredAt());
	}
}
