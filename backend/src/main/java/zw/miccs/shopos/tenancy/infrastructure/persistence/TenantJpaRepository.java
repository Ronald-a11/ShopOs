package zw.miccs.shopos.tenancy.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantJpaRepository extends JpaRepository<TenantEntity, UUID> {

	Optional<TenantEntity> findByName(String name);

}
