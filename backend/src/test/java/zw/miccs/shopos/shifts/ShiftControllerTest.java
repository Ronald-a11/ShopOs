package zw.miccs.shopos.shifts;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class ShiftControllerTest {

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
	void openCashCloseAndRejectSecondOpenShift() throws Exception {
		Cookie access = login();
		String branchId = id(mockMvc.perform(post("/api/v1/branches")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"BR-200","name":"Shift Branch"}
					"""))
			.andExpect(status().isCreated())
			.andReturn());
		String deviceId = id(mockMvc.perform(post("/api/v1/branches/" + branchId + "/devices")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"T-200","name":"Till"}
					"""))
			.andExpect(status().isCreated())
			.andReturn());

		MvcResult opened = mockMvc.perform(post("/api/v1/shifts/open")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"branchId":"%s","deviceId":"%s","openingFloat":100.00}
					""".formatted(branchId, deviceId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("OPEN"))
			.andExpect(jsonPath("$.openingFloat").value(100.00))
			.andReturn();
		String shiftId = json(opened).get("id").asText();

		mockMvc.perform(post("/api/v1/shifts/open")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"branchId":"%s","deviceId":"%s","openingFloat":10.00}
					""".formatted(branchId, deviceId)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SHIFT_ALREADY_OPEN"));

		mockMvc.perform(get("/api/v1/shifts/current").cookie(access).param("deviceId", deviceId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(shiftId));

		mockMvc.perform(post("/api/v1/shifts/" + shiftId + "/cash-movements")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"type":"PAID_IN","amount":20.00,"note":"Float top-up"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.type").value("PAID_IN"));

		mockMvc.perform(post("/api/v1/shifts/" + shiftId + "/close")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"countedCash":120.00,"note":"Balanced"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CLOSED"))
			.andExpect(jsonPath("$.expectedCash").value(120.00))
			.andExpect(jsonPath("$.variance").value(0));

		mockMvc.perform(get("/api/v1/shifts/current").cookie(access).param("deviceId", deviceId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("SHIFT_NOT_OPEN"));

		mockMvc.perform(get("/api/v1/reports/cash-reconciliation").cookie(access).param("shiftId", shiftId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paidIn").value(20.00))
			.andExpect(jsonPath("$.expectedCash").value(120.00));

		mockMvc.perform(post("/api/v1/shifts/open")
				.cookie(access)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"branchId":"%s","deviceId":"%s","openingFloat":50.00}
					""".formatted(branchId, deviceId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("OPEN"));
	}

	@Test
	void unauthenticatedShiftOpenIsRejected() throws Exception {
		mockMvc.perform(post("/api/v1/shifts/open")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"branchId":"00000000-0000-0000-0000-000000000001","deviceId":"00000000-0000-0000-0000-000000000002","openingFloat":0}
					"""))
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

	private String id(MvcResult result) throws Exception {
		return json(result).get("id").asText();
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
