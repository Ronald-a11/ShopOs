package zw.miccs.shopos.branches;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
class BranchControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

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

	private final ObjectMapper objectMapper = new ObjectMapper();

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
	void createListUpdateBranchAndDevice() throws Exception {
		Cookie access = login();

		MvcResult created = mockMvc.perform(post("/api/v1/branches")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"BR-100","name":"Test Branch","address":"First Street"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("BR-100"))
			.andExpect(jsonPath("$.status").value("ACTIVE"))
			.andReturn();
		String branchId = json(created).get("id").asText();

		mockMvc.perform(get("/api/v1/branches").cookie(access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.code == 'BR-100')].name").value("Test Branch"));

		mockMvc.perform(get("/api/v1/branches/" + branchId).cookie(access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.address").value("First Street"));

		mockMvc.perform(patch("/api/v1/branches/" + branchId)
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Test Branch Updated","status":"INACTIVE"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Test Branch Updated"))
			.andExpect(jsonPath("$.status").value("INACTIVE"));

		mockMvc.perform(post("/api/v1/branches")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"BR-100","name":"Duplicate"}
					"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("DUPLICATE_BRANCH_CODE"));

		mockMvc.perform(post("/api/v1/branches/" + branchId + "/devices")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"T-100","name":"Counter","platform":"DESKTOP"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("AVAILABLE"))
			.andExpect(jsonPath("$.branchId").value(branchId));

		mockMvc.perform(get("/api/v1/branches/" + branchId + "/devices").cookie(access))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].code").value("T-100"));
	}

	@Test
	void unauthenticatedBranchListIsRejected() throws Exception {
		mockMvc.perform(get("/api/v1/branches"))
			.andExpect(status().isUnauthorized());
	}

	private Cookie login() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"cashier@demo.shopos.co.zw","password":"Password1!","rememberMe":false}
					"""))
			.andExpect(status().isOk())
			.andReturn();
		return cookie(login, "shopos_at");
	}

	private JsonNode json(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString());
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
