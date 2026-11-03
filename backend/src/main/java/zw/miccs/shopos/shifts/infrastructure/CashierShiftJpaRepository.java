package zw.miccs.shopos.shifts.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import zw.miccs.shopos.shifts.domain.ShiftStatus;

public interface CashierShiftJpaRepository extends JpaRepository<CashierShiftEntity, CashierShiftEntity.Pk> {

	Optional<CashierShiftEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<CashierShiftEntity> findByTenantIdAndCashierUserIdAndDeviceIdAndStatus(
		UUID tenantId,
		UUID cashierUserId,
		UUID deviceId,
		ShiftStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select s from CashierShiftEntity s
		where s.tenantId = :tenantId and s.id = :id
		""")
	Optional<CashierShiftEntity> findForUpdate(@Param("tenantId") UUID tenantId, @Param("id") UUID id);
}
