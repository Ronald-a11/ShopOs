package zw.miccs.shopos.shifts.application;

import java.util.Optional;
import java.util.UUID;

/**
 * Public shift reads. Reporting uses {@link #cashReconciliation}; sales uses {@link #requireShift}.
 */
public interface ShiftQueryService {

	ShiftView requireShift(UUID tenantId, UUID shiftId);

	Optional<ShiftView> findOpenShiftForDevice(UUID tenantId, UUID deviceId);

	CashReconciliationView cashReconciliation(UUID tenantId, UUID shiftId);

}
