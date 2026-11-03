package zw.miccs.shopos.shifts.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftCashMovementJpaRepository
	extends JpaRepository<ShiftCashMovementEntity, ShiftCashMovementEntity.Pk> {

	List<ShiftCashMovementEntity> findByTenantIdAndShiftIdOrderByOccurredAtAsc(UUID tenantId, UUID shiftId);

	Optional<ShiftCashMovementEntity> findByTenantIdAndId(UUID tenantId, UUID id);
}
