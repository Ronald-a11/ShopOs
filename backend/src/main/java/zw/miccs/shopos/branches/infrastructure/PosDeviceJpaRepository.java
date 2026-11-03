package zw.miccs.shopos.branches.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PosDeviceJpaRepository extends JpaRepository<PosDeviceEntity, PosDeviceEntity.Pk> {

	List<PosDeviceEntity> findByTenantIdAndBranchIdOrderByNameAsc(UUID tenantId, UUID branchId);

	Optional<PosDeviceEntity> findByTenantIdAndBranchIdAndId(UUID tenantId, UUID branchId, UUID id);
}
