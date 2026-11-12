package zw.miccs.shopos.fiscalisation.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalDocumentJpaRepository extends JpaRepository<FiscalDocumentEntity, UUID> {

	Optional<FiscalDocumentEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<FiscalDocumentEntity> findByTenantIdAndSaleId(UUID tenantId, UUID saleId);

}
