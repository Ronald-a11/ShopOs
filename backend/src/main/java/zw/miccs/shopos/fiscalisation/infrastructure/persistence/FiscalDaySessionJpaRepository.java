package zw.miccs.shopos.fiscalisation.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalDaySessionJpaRepository extends JpaRepository<FiscalDaySessionEntity, UUID> {

	Optional<FiscalDaySessionEntity> findByTenantIdAndStatus(UUID tenantId, String status);

}
