package zw.miccs.shopos.inventory.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryOperationJpaRepository
	extends JpaRepository<InventoryOperationEntity, InventoryOperationEntity.Pk> {

	Optional<InventoryOperationEntity> findByTenantIdAndClientOperationId(UUID tenantId, UUID clientOperationId);
}
