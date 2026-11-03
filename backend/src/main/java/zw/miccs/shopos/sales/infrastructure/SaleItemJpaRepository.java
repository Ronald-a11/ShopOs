package zw.miccs.shopos.sales.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemJpaRepository extends JpaRepository<SaleItemEntity, SaleItemEntity.Pk> {

	List<SaleItemEntity> findByTenantIdAndSaleIdOrderByCreatedAtAsc(UUID tenantId, UUID saleId);
}
