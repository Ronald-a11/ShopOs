package zw.miccs.shopos.tenancy.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.tenancy.domain.TenantRole;
import zw.miccs.shopos.tenancy.infrastructure.persistence.MembershipEntity;
import zw.miccs.shopos.tenancy.infrastructure.persistence.MembershipJpaRepository;
import zw.miccs.shopos.tenancy.infrastructure.persistence.TenantEntity;
import zw.miccs.shopos.tenancy.infrastructure.persistence.TenantJpaRepository;

@Service
public class MembershipQueryServiceImpl implements MembershipQueryService, MembershipProvisioningService {

	private final MembershipJpaRepository memberships;
	private final TenantJpaRepository tenants;

	public MembershipQueryServiceImpl(MembershipJpaRepository memberships, TenantJpaRepository tenants) {
		this.memberships = memberships;
		this.tenants = tenants;
	}

	@Override
	@Transactional
	public void createActiveTenantAndMembership(UUID userId, String tenantName, TenantRole role, Instant createdAt) {
		TenantEntity tenant = tenants.findByName(tenantName).orElseGet(() -> {
			UUID tenantId = UUID.randomUUID();
			return tenants.save(new TenantEntity(tenantId, tenantName, "ACTIVE", createdAt));
		});
		memberships.save(new MembershipEntity(
			UUID.randomUUID(),
			tenant.getId(),
			userId,
			role.name(),
			"ACTIVE",
			createdAt));
	}

	@Override
	@Transactional(readOnly = true)
	public List<MembershipView> findActiveMembershipsForUser(UUID userId) {
		List<MembershipEntity> rows = memberships.findByUserIdAndStatus(userId, "ACTIVE");
		return rows.stream()
			.map(row -> {
				TenantEntity tenant = tenants.findById(row.getTenantId())
					.orElseThrow(() -> new IllegalStateException("Membership references missing tenant"));
				if (!"ACTIVE".equals(tenant.getStatus())) {
					return null;
				}
				return new MembershipView(
					row.getId(),
					tenant.getId(),
					tenant.getName(),
					TenantRole.valueOf(row.getRole()));
			})
			.filter(view -> view != null)
			.toList();
	}

}
