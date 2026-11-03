package zw.miccs.shopos.shifts.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftOperationJpaRepository extends JpaRepository<ShiftOperationEntity, ShiftOperationEntity.Pk> {

	Optional<ShiftOperationEntity> findByTenantIdAndClientOperationId(UUID tenantId, UUID clientOperationId);
}
