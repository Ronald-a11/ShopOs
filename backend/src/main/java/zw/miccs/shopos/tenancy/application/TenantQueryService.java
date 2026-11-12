package zw.miccs.shopos.tenancy.application;

import java.util.Optional;
import java.util.UUID;

public interface TenantQueryService {

	Optional<UUID> findActiveTenantIdByName(String name);

}
