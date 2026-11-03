package zw.miccs.shopos.sales.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalePaymentJpaRepository extends JpaRepository<SalePaymentEntity, SalePaymentEntity.Pk> {

	List<SalePaymentEntity> findByTenantIdAndSaleIdOrderByCreatedAtAsc(UUID tenantId, UUID saleId);
}
