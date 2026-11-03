package zw.miccs.shopos.inventory.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryMovementJpaRepository extends JpaRepository<InventoryMovementEntity, InventoryMovementEntity.Pk> {

	@Query("""
		select m from InventoryMovementEntity m
		where m.tenantId = :tenantId
		  and m.branchId = :branchId
		  and (:productId is null or m.productId = :productId)
		""")
	Page<InventoryMovementEntity> findByBranch(
		@Param("tenantId") UUID tenantId,
		@Param("branchId") UUID branchId,
		@Param("productId") UUID productId,
		Pageable pageable);

	List<InventoryMovementEntity> findByTenantIdAndReferenceTypeAndReferenceIdOrderByOccurredAtAsc(
		UUID tenantId,
		String referenceType,
		UUID referenceId);
}
