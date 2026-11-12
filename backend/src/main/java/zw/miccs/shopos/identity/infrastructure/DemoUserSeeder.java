package zw.miccs.shopos.identity.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.identity.domain.UserStatus;
import zw.miccs.shopos.identity.infrastructure.persistence.UserCredentialEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.UserCredentialJpaRepository;
import zw.miccs.shopos.identity.infrastructure.persistence.UserEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.UserJpaRepository;
import zw.miccs.shopos.tenancy.application.MembershipProvisioningService;
import zw.miccs.shopos.tenancy.domain.TenantRole;

/**
 * Seeds demo users for every tenant role from the technical documentation
 * when {@code shopos.auth.seed-demo-user=true}. Shared password is never logged.
 */
@Component
@Order(0)
public class DemoUserSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);
	private static final String TENANT_NAME = "Demo Grocers";
	private static final String DEMO_PASSWORD = "Password1!";

	private static final List<DemoAccount> ACCOUNTS = List.of(
		new DemoAccount("owner@demo.shopos.co.zw", "Demo Owner", "+263771000001", TenantRole.OWNER),
		new DemoAccount("admin@demo.shopos.co.zw", "Demo Admin", "+263771000002", TenantRole.ADMIN),
		new DemoAccount("manager@demo.shopos.co.zw", "Demo Manager", "+263771000003", TenantRole.MANAGER),
		new DemoAccount("supervisor@demo.shopos.co.zw", "Demo Supervisor", "+263771000004", TenantRole.SUPERVISOR),
		new DemoAccount("cashier@demo.shopos.co.zw", "Demo Cashier", "+263771000005", TenantRole.CASHIER),
		new DemoAccount("stock@demo.shopos.co.zw", "Demo Stock Controller", "+263771000006", TenantRole.STOCK_CONTROLLER)
	);

	private final boolean enabled;
	private final UserJpaRepository users;
	private final UserCredentialJpaRepository credentials;
	private final MembershipProvisioningService memberships;
	private final PasswordEncoder passwordEncoder;

	public DemoUserSeeder(
		@Value("${shopos.auth.seed-demo-user:false}") boolean enabled,
		UserJpaRepository users,
		UserCredentialJpaRepository credentials,
		MembershipProvisioningService memberships,
		PasswordEncoder passwordEncoder
	) {
		this.enabled = enabled;
		this.users = users;
		this.credentials = credentials;
		this.memberships = memberships;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!enabled) {
			return;
		}

		Instant now = Instant.now();
		int created = 0;
		for (DemoAccount account : ACCOUNTS) {
			if (users.findByEmail(account.email()).isPresent()) {
				continue;
			}
			UUID userId = UUID.randomUUID();
			users.save(new UserEntity(
				userId,
				account.email(),
				account.displayName(),
				account.phone(),
				UserStatus.ACTIVE.name(),
				now,
				null,
				now,
				now));
			credentials.save(new UserCredentialEntity(userId, passwordEncoder.encode(DEMO_PASSWORD), now));
			memberships.createActiveTenantAndMembership(userId, TENANT_NAME, account.role(), now);
			created++;
		}

		if (created > 0) {
			log.info("Seeded {} demo auth user(s) for tenant '{}' (password set; not logged)", created, TENANT_NAME);
		}
	}

	private record DemoAccount(String email, String displayName, String phone, TenantRole role) {
	}

}
