package zw.miccs.shopos.shifts.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashierShiftJpaRepository extends JpaRepository<CashierShiftEntity, UUID> {

	Optional<CashierShiftEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<CashierShiftEntity> findByTenantIdAndDeviceIdAndStatus(UUID tenantId, UUID deviceId, String status);

}
