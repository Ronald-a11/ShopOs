package zw.miccs.shopos.inventory.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryBalanceJpaRepository extends JpaRepository<InventoryBalanceEntity, InventoryBalanceId> {

	List<InventoryBalanceEntity> findByIdTenantIdAndIdBranchIdOrderByIdProductIdAsc(UUID tenantId, UUID branchId);

	@Query("""
		SELECT b FROM InventoryBalanceEntity b
		WHERE b.id.tenantId = :tenantId
		  AND (:branchId IS NULL OR b.id.branchId = :branchId)
		  AND b.quantity <= :threshold
		ORDER BY b.quantity ASC
		""")
	List<InventoryBalanceEntity> findLowStock(
		@Param("tenantId") UUID tenantId,
		@Param("branchId") UUID branchId,
		@Param("threshold") BigDecimal threshold
	);

}
