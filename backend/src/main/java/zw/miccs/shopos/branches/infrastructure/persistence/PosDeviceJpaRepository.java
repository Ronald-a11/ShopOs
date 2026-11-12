package zw.miccs.shopos.branches.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PosDeviceJpaRepository extends JpaRepository<PosDeviceEntity, UUID> {

	List<PosDeviceEntity> findByTenantIdAndBranchIdOrderByCodeAsc(UUID tenantId, UUID branchId);

	Optional<PosDeviceEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<PosDeviceEntity> findByTenantIdAndCode(UUID tenantId, String code);

}
