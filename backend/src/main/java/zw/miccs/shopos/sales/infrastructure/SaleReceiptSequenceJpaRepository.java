package zw.miccs.shopos.sales.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SaleReceiptSequenceJpaRepository
	extends JpaRepository<SaleReceiptSequenceEntity, SaleReceiptSequenceEntity.Pk> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select s from SaleReceiptSequenceEntity s
		where s.tenantId = :tenantId and s.branchId = :branchId
		""")
	Optional<SaleReceiptSequenceEntity> findForUpdate(
		@Param("tenantId") UUID tenantId,
		@Param("branchId") UUID branchId);
}
