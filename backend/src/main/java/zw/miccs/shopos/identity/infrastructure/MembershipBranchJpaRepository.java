package zw.miccs.shopos.identity.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipBranchJpaRepository extends JpaRepository<MembershipBranchEntity, MembershipBranchEntity.Pk> {

	List<MembershipBranchEntity> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
