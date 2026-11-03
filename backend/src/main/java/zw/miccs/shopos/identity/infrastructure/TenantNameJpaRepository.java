package zw.miccs.shopos.identity.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantNameJpaRepository extends JpaRepository<TenantNameEntity, UUID> {
}
