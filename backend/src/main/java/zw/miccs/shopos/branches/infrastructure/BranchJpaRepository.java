package zw.miccs.shopos.branches.infrastructure;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchJpaRepository extends JpaRepository<BranchEntity, BranchEntity.Pk> {

	Page<BranchEntity> findByTenantId(UUID tenantId, Pageable pageable);

	Page<BranchEntity> findByTenantIdAndIdIn(UUID tenantId, Collection<UUID> ids, Pageable pageable);

	Optional<BranchEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	boolean existsByTenantIdAndCode(UUID tenantId, String code);
}
