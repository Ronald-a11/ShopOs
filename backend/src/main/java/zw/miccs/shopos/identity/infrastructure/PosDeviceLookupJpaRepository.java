package zw.miccs.shopos.identity.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PosDeviceLookupJpaRepository extends JpaRepository<PosDeviceLookupEntity, PosDeviceLookupEntity.Pk> {

	@Query("select d from PosDeviceLookupEntity d where d.id = :deviceId and d.status = 'ACTIVE'")
	Optional<PosDeviceLookupEntity> findActiveById(@Param("deviceId") UUID deviceId);
}
