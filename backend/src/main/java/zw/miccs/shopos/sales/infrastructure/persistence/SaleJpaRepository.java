package zw.miccs.shopos.sales.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SaleJpaRepository extends JpaRepository<SaleEntity, UUID> {

	Optional<SaleEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<SaleEntity> findByTenantIdAndDeviceIdAndClientSaleId(UUID tenantId, UUID deviceId, String clientSaleId);

	List<SaleEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

	List<SaleEntity> findByTenantIdAndBranchIdOrderByCreatedAtDesc(UUID tenantId, UUID branchId);

	@Query("""
		SELECT s FROM SaleEntity s
		WHERE s.tenantId = :tenantId
		  AND (:branchId IS NULL OR s.branchId = :branchId)
		  AND (:from IS NULL OR s.createdAt >= :from)
		  AND (:to IS NULL OR s.createdAt < :to)
		  AND s.status IN ('COMPLETED', 'VOID_REQUESTED')
		""")
	List<SaleEntity> findForReport(
		@Param("tenantId") UUID tenantId,
		@Param("branchId") UUID branchId,
		@Param("from") Instant from,
		@Param("to") Instant to
	);

}
