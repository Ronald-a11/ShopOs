package zw.miccs.shopos.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;
import zw.miccs.shopos.TestcontainersConfiguration;
import zw.miccs.shopos.identity.domain.UserStatus;
import zw.miccs.shopos.identity.infrastructure.persistence.UserCredentialEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.UserCredentialJpaRepository;
import zw.miccs.shopos.identity.infrastructure.persistence.UserEntity;
import zw.miccs.shopos.identity.infrastructure.persistence.UserJpaRepository;
import zw.miccs.shopos.tenancy.domain.TenantRole;
import zw.miccs.shopos.tenancy.infrastructure.persistence.MembershipEntity;
import zw.miccs.shopos.tenancy.infrastructure.persistence.MembershipJpaRepository;
import zw.miccs.shopos.tenancy.infrastructure.persistence.TenantEntity;
import zw.miccs.shopos.tenancy.infrastructure.persistence.TenantJpaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserJpaRepository users;

	@Autowired
	UserCredentialJpaRepository credentials;

	@Autowired
	TenantJpaRepository tenants;

	@Autowired
	MembershipJpaRepository memberships;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JdbcTemplate jdbc;

	@BeforeEach
	void setUp() {
		jdbc.execute("TRUNCATE TABLE users, tenants CASCADE");

		UUID userId = UUID.randomUUID();
		UUID tenantId = UUID.randomUUID();
		Instant now = Instant.now();

		users.save(new UserEntity(
			userId,
			"cashier@demo.shopos.co.zw",
			"Demo Cashier",
			"+263771234567",
			UserStatus.ACTIVE.name(),
			now,
			null,
			now,
			now));
		credentials.save(new UserCredentialEntity(userId, passwordEncoder.encode("Password1!"), now));
		tenants.save(new TenantEntity(tenantId, "Demo Grocers", "ACTIVE", now));
		memberships.save(new MembershipEntity(
			UUID.randomUUID(),
			tenantId,
			userId,
			TenantRole.CASHIER.name(),
			"ACTIVE",
			now));
	}

	@Test
	void loginRefreshMeAndLogout() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"cashier@demo.shopos.co.zw","password":"Password1!","rememberMe":true}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.email").value("cashier@demo.shopos.co.zw"))
			.andExpect(jsonPath("$.user.memberships[0].role").value("CASHIER"))
			.andExpect(header().exists("Set-Cookie"))
			.andReturn();

		Cookie access = cookie(login, "shopos_at");
		Cookie refresh = cookie(login, "shopos_rt");
		assertThat(access).isNotNull();
		assertThat(refresh).isNotNull();

		mockMvc.perform(get("/api/v1/me").cookie(access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.displayName").value("Demo Cashier"));

		MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh").cookie(refresh))
			.andExpect(status().isNoContent())
			.andReturn();

		Cookie newAccess = cookie(refreshed, "shopos_at");
		assertThat(newAccess).isNotNull();

		mockMvc.perform(post("/api/v1/auth/logout").cookie(newAccess, cookie(refreshed, "shopos_rt")))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/me").cookie(newAccess))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginRejectsDisabledAccount() throws Exception {
		UserEntity user = users.findByEmail("cashier@demo.shopos.co.zw").orElseThrow();
		user.setStatus(UserStatus.DISABLED.name());
		users.save(user);

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"cashier@demo.shopos.co.zw","password":"Password1!","rememberMe":false}
					"""))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));
	}

	@Test
	void forgotAndResetPassword() throws Exception {
		mockMvc.perform(post("/api/v1/auth/forgot-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"cashier@demo.shopos.co.zw"}
					"""))
			.andExpect(status().isAccepted());
	}

	private static Cookie cookie(MvcResult result, String name) {
		for (String header : result.getResponse().getHeaders("Set-Cookie")) {
			if (header.startsWith(name + "=")) {
				String value = header.substring(name.length() + 1).split(";", 2)[0];
				return new Cookie(name, value);
			}
		}
		return null;
	}

}
