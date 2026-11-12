package zw.miccs.shopos.tenancy.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipJpaRepository extends JpaRepository<MembershipEntity, UUID> {

	List<MembershipEntity> findByUserIdAndStatus(UUID userId, String status);

}
