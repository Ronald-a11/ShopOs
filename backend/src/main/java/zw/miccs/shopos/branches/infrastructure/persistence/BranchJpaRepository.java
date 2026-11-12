package zw.miccs.shopos.branches.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchJpaRepository extends JpaRepository<BranchEntity, UUID> {

	List<BranchEntity> findByTenantIdOrderByCodeAsc(UUID tenantId);

	Optional<BranchEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<BranchEntity> findByTenantIdAndCode(UUID tenantId, String code);

}
