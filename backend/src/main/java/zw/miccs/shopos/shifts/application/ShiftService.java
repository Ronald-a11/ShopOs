package zw.miccs.shopos.shifts.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.application.BranchQueryService;
import zw.miccs.shopos.branches.application.BranchView;
import zw.miccs.shopos.branches.application.DeviceView;
import zw.miccs.shopos.branches.domain.BranchStatus;
import zw.miccs.shopos.common.ApiException;
import zw.miccs.shopos.common.Enums;
import zw.miccs.shopos.shifts.domain.CashMovementType;
import zw.miccs.shopos.shifts.domain.ShiftStatus;
import zw.miccs.shopos.shifts.infrastructure.persistence.CashMovementEntity;
import zw.miccs.shopos.shifts.infrastructure.persistence.CashMovementJpaRepository;
import zw.miccs.shopos.shifts.infrastructure.persistence.CashierShiftEntity;
import zw.miccs.shopos.shifts.infrastructure.persistence.CashierShiftJpaRepository;

@Service
public class ShiftService implements ShiftQueryService {

	private final CashierShiftJpaRepository shifts;
	private final CashMovementJpaRepository movements;
	private final BranchQueryService branches;

	public ShiftService(
		CashierShiftJpaRepository shifts,
		CashMovementJpaRepository movements,
		BranchQueryService branches
	) {
		this.shifts = shifts;
		this.movements = movements;
		this.branches = branches;
	}

	@Override
	@Transactional(readOnly = true)
	public ShiftView requireShift(UUID tenantId, UUID shiftId) {
		return shifts.findByTenantIdAndId(tenantId, shiftId)
			.map(ShiftService::toView)
			.orElseThrow(() -> notFound());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ShiftView> findOpenShiftForDevice(UUID tenantId, UUID deviceId) {
		return shifts.findByTenantIdAndDeviceIdAndStatus(tenantId, deviceId, ShiftStatus.OPEN.name())
			.map(ShiftService::toView);
	}

	@Override
	@Transactional(readOnly = true)
	public CashReconciliationView cashReconciliation(UUID tenantId, UUID shiftId) {
		CashierShiftEntity shift = requireEntity(tenantId, shiftId);
		Totals totals = totals(tenantId, shift);
		BigDecimal expected = shift.getExpectedCash() != null ? shift.getExpectedCash() : totals.expected(shift);
		BigDecimal variance = shift.getVariance();
		if (variance == null && shift.getCountedCash() != null) {
			variance = money(shift.getCountedCash().subtract(expected));
		}
		return new CashReconciliationView(
			shift.getId(),
			shift.getBranchId(),
			shift.getDeviceId(),
			shift.getStatus(),
			shift.getOpeningFloat(),
			totals.paidIn(),
			totals.paidOut(),
			totals.drops(),
			expected,
			shift.getCountedCash(),
			variance,
			shift.getNote(),
			shift.getOpenedAt(),
			shift.getClosedAt());
	}

	@Transactional
	public ShiftView open(UUID tenantId, UUID userId, UUID branchId, UUID deviceId, BigDecimal openingFloat) {
		BranchView branch = branches.requireBranch(tenantId, branchId);
		if (!BranchStatus.ACTIVE.name().equals(branch.status())) {
			throw new ApiException(HttpStatus.CONFLICT, "BRANCH_INACTIVE", "Branch inactive",
				"Branch is not active");
		}
		DeviceView device = branches.requireDevice(tenantId, deviceId);
		if (!device.branchId().equals(branchId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "DEVICE_BRANCH_MISMATCH", "Device branch mismatch",
				"Device does not belong to the branch");
		}
		if (openingFloat == null || openingFloat.signum() < 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"openingFloat must be zero or greater");
		}
		if (shifts.findByTenantIdAndDeviceIdAndStatus(tenantId, deviceId, ShiftStatus.OPEN.name()).isPresent()) {
			throw alreadyOpen();
		}
		CashierShiftEntity entity = new CashierShiftEntity(
			UUID.randomUUID(),
			tenantId,
			branchId,
			deviceId,
			userId,
			ShiftStatus.OPEN.name(),
			money(openingFloat),
			Instant.now());
		try {
			return toView(shifts.saveAndFlush(entity));
		}
		catch (DataIntegrityViolationException ex) {
			throw alreadyOpen();
		}
	}

	@Transactional
	public CashMovementView addCashMovement(
		UUID tenantId,
		UUID shiftId,
		String type,
		BigDecimal amount,
		String note
	) {
		CashierShiftEntity shift = requireOpen(tenantId, shiftId);
		CashMovementType movementType = Enums.parse(CashMovementType.class, type, "type");
		if (amount == null || amount.signum() <= 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"amount must be greater than zero");
		}
		CashMovementEntity entity = new CashMovementEntity(
			UUID.randomUUID(),
			tenantId,
			shift.getId(),
			movementType.name(),
			money(amount),
			blankToNull(note),
			Instant.now());
		movements.save(entity);
		return toView(entity);
	}

	@Transactional
	public ShiftView close(UUID tenantId, UUID shiftId, BigDecimal countedCash, String note) {
		CashierShiftEntity shift = requireOpen(tenantId, shiftId);
		if (countedCash == null || countedCash.signum() < 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"countedCash must be zero or greater");
		}
		Totals totals = totals(tenantId, shift);
		BigDecimal expected = totals.expected(shift);
		BigDecimal counted = money(countedCash);
		shift.setStatus(ShiftStatus.CLOSED.name());
		shift.setCountedCash(counted);
		shift.setExpectedCash(expected);
		shift.setVariance(money(counted.subtract(expected)));
		shift.setNote(blankToNull(note));
		shift.setClosedAt(Instant.now());
		return toView(shifts.save(shift));
	}

	private CashierShiftEntity requireOpen(UUID tenantId, UUID shiftId) {
		CashierShiftEntity shift = requireEntity(tenantId, shiftId);
		if (!ShiftStatus.OPEN.name().equals(shift.getStatus())) {
			throw new ApiException(HttpStatus.CONFLICT, "SHIFT_NOT_OPEN", "Shift not open",
				"Shift is not open");
		}
		return shift;
	}

	private CashierShiftEntity requireEntity(UUID tenantId, UUID shiftId) {
		return shifts.findByTenantIdAndId(tenantId, shiftId).orElseThrow(ShiftService::notFound);
	}

	private Totals totals(UUID tenantId, CashierShiftEntity shift) {
		List<CashMovementEntity> rows = movements.findByTenantIdAndShiftIdOrderByCreatedAtAsc(tenantId, shift.getId());
		BigDecimal paidIn = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		BigDecimal paidOut = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		BigDecimal drops = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		for (CashMovementEntity row : rows) {
			if (CashMovementType.PAID_IN.name().equals(row.getType())) {
				paidIn = paidIn.add(row.getAmount());
			}
			else if (CashMovementType.PAID_OUT.name().equals(row.getType())) {
				paidOut = paidOut.add(row.getAmount());
			}
			else if (CashMovementType.DROP.name().equals(row.getType())) {
				drops = drops.add(row.getAmount());
			}
		}
		return new Totals(money(paidIn), money(paidOut), money(drops));
	}

	private static ShiftView toView(CashierShiftEntity entity) {
		return new ShiftView(
			entity.getId(),
			entity.getTenantId(),
			entity.getBranchId(),
			entity.getDeviceId(),
			entity.getUserId(),
			entity.getStatus(),
			entity.getOpeningFloat(),
			entity.getCountedCash(),
			entity.getExpectedCash(),
			entity.getVariance(),
			entity.getNote(),
			entity.getOpenedAt(),
			entity.getClosedAt());
	}

	private static CashMovementView toView(CashMovementEntity entity) {
		return new CashMovementView(
			entity.getId(),
			entity.getShiftId(),
			entity.getType(),
			entity.getAmount(),
			entity.getNote(),
			entity.getCreatedAt());
	}

	private static BigDecimal money(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static ApiException notFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "SHIFT_NOT_FOUND", "Shift not found", "Shift does not exist");
	}

	private static ApiException alreadyOpen() {
		return new ApiException(HttpStatus.CONFLICT, "SHIFT_ALREADY_OPEN", "Shift already open",
			"This device already has an open shift");
	}

	private record Totals(BigDecimal paidIn, BigDecimal paidOut, BigDecimal drops) {

		private BigDecimal expected(CashierShiftEntity shift) {
			return money(shift.getOpeningFloat().add(paidIn).subtract(paidOut).subtract(drops));
		}

	}

}
