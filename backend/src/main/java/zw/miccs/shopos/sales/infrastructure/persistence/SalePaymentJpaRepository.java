package zw.miccs.shopos.sales.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalePaymentJpaRepository extends JpaRepository<SalePaymentEntity, UUID> {

	List<SalePaymentEntity> findByTenantIdAndSaleId(UUID tenantId, UUID saleId);

}
