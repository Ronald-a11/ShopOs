package zw.miccs.shopos.tenancy.application;

import java.util.List;
import java.util.UUID;

/**
 * Public tenancy interface used by identity (and later other modules) for membership lookups.
 */
public interface MembershipQueryService {

	List<MembershipView> findActiveMembershipsForUser(UUID userId);

}
