package zw.miccs.shopos.shifts.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashMovementJpaRepository extends JpaRepository<CashMovementEntity, UUID> {

	List<CashMovementEntity> findByTenantIdAndShiftIdOrderByCreatedAtAsc(UUID tenantId, UUID shiftId);

}
