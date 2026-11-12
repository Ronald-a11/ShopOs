package zw.miccs.shopos.tenancy.application;

import java.util.UUID;

/**
 * Resolves the tenant a request should operate on. P0 uses the user's earliest active membership.
 */
public interface CurrentTenantService {

	UUID requirePrimaryTenantId(UUID userId);

}
