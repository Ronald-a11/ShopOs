package zw.miccs.shopos.inventory.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface InventoryBalanceJpaRepository extends JpaRepository<InventoryBalanceEntity, InventoryBalanceEntity.Pk> {

	Page<InventoryBalanceEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId, Pageable pageable);

	@Query("""
		select b from InventoryBalanceEntity b
		where b.tenantId = :tenantId
		  and b.branchId = :branchId
		  and b.reorderLevel is not null
		  and b.quantity <= b.reorderLevel
		""")
	Page<InventoryBalanceEntity> findLowStock(
		@Param("tenantId") UUID tenantId,
		@Param("branchId") UUID branchId,
		Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select b from InventoryBalanceEntity b
		where b.tenantId = :tenantId
		  and b.branchId = :branchId
		  and b.productId = :productId
		""")
	Optional<InventoryBalanceEntity> findForUpdate(
		@Param("tenantId") UUID tenantId,
		@Param("branchId") UUID branchId,
		@Param("productId") UUID productId);
}
