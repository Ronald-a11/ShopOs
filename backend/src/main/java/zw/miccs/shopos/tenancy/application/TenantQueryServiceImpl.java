package zw.miccs.shopos.tenancy.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.tenancy.infrastructure.persistence.TenantJpaRepository;

@Service
public class TenantQueryServiceImpl implements TenantQueryService {

	private final TenantJpaRepository tenants;

	public TenantQueryServiceImpl(TenantJpaRepository tenants) {
		this.tenants = tenants;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<UUID> findActiveTenantIdByName(String name) {
		return tenants.findByName(name)
			.filter(tenant -> "ACTIVE".equals(tenant.getStatus()))
			.map(tenant -> tenant.getId());
	}

}
