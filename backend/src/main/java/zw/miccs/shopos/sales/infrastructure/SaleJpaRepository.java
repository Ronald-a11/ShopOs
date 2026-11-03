package zw.miccs.shopos.sales.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleJpaRepository extends JpaRepository<SaleEntity, SaleEntity.Pk> {

	Optional<SaleEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<SaleEntity> findByTenantIdAndDeviceIdAndClientSaleId(UUID tenantId, UUID deviceId, UUID clientSaleId);

	List<SaleEntity> findByTenantIdAndOriginalSaleId(UUID tenantId, UUID originalSaleId);

	@Query("""
		select s from SaleEntity s
		where s.tenantId = :tenantId
		  and (:branchId is null or s.branchId = :branchId)
		  and (:cashierUserId is null or s.cashierUserId = :cashierUserId)
		  and s.occurredAt >= :from
		  and s.occurredAt <= :to
		  and s.originalSaleId is null
		""")
	Page<SaleEntity> search(
		@Param("tenantId") UUID tenantId,
		@Param("branchId") UUID branchId,
		@Param("cashierUserId") UUID cashierUserId,
		@Param("from") Instant from,
		@Param("to") Instant to,
		Pageable pageable);
}
