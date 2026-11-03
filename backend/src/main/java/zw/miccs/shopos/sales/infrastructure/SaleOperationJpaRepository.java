package zw.miccs.shopos.sales.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleOperationJpaRepository extends JpaRepository<SaleOperationEntity, SaleOperationEntity.Pk> {

	Optional<SaleOperationEntity> findByTenantIdAndClientOperationId(UUID tenantId, UUID clientOperationId);
}
