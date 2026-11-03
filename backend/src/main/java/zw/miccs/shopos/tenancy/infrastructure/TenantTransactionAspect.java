package zw.miccs.shopos.tenancy.infrastructure;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import zw.miccs.shopos.tenancy.application.TenantContext;

@Aspect
@Component
public class TenantTransactionAspect {

	private final JdbcTemplate jdbcTemplate;

	public TenantTransactionAspect(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Before("@within(org.springframework.transaction.annotation.Transactional) || @annotation(org.springframework.transaction.annotation.Transactional)")
	public void applyTenantSetting() {
		if (!TransactionSynchronizationManager.isActualTransactionActive()) {
			return;
		}
		try {
			var holder = TenantContext.require();
			jdbcTemplate.queryForObject(
				"SELECT set_config('app.tenant_id', ?, true)",
				String.class,
				holder.tenantId().toString());
		}
		catch (IllegalStateException ignored) {
			// No tenant context (e.g. startup); leave unset.
		}
	}
}
