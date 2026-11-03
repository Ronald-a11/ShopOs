package zw.miccs.shopos.identity.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantMembershipJpaRepository extends JpaRepository<TenantMembershipEntity, TenantMembershipEntity.Pk> {

	List<TenantMembershipEntity> findByUserIdOrderByCreatedAtAsc(UUID userId);

	Optional<TenantMembershipEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
