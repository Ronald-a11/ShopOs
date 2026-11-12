package zw.miccs.shopos.tenancy.application;

import java.util.Comparator;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.common.ApiException;

@Service
public class CurrentTenantServiceImpl implements CurrentTenantService {

	private final MembershipQueryService memberships;

	public CurrentTenantServiceImpl(MembershipQueryService memberships) {
		this.memberships = memberships;
	}

	@Override
	@Transactional(readOnly = true)
	public UUID requirePrimaryTenantId(UUID userId) {
		return memberships.findActiveMembershipsForUser(userId).stream()
			.min(Comparator.comparing(MembershipView::membershipId))
			.map(MembershipView::tenantId)
			.orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NO_TENANT", "No tenant",
				"User has no active tenant membership"));
	}

}
