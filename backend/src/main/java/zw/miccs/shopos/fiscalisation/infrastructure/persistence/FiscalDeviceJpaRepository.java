package zw.miccs.shopos.fiscalisation.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalDeviceJpaRepository extends JpaRepository<FiscalDeviceEntity, UUID> {

	List<FiscalDeviceEntity> findByTenantIdOrderByCodeAsc(UUID tenantId);

	Optional<FiscalDeviceEntity> findByTenantIdAndCode(UUID tenantId, String code);

}
