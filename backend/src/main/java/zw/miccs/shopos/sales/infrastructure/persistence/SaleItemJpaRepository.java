package zw.miccs.shopos.sales.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemJpaRepository extends JpaRepository<SaleItemEntity, UUID> {

	List<SaleItemEntity> findByTenantIdAndSaleId(UUID tenantId, UUID saleId);

}
