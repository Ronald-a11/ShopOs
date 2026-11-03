package zw.miccs.shopos.identity;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import zw.miccs.shopos.TestcontainersConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthApiIntegrationTest {

	private static final UUID TENANT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID TENANT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID USER_DISABLED = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID BRANCH_A = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID BRANCH_B = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private static final UUID DEVICE_A = UUID.fromString("55555555-5555-5555-5555-555555555555");
	private static final UUID DEVICE_B = UUID.fromString("66666666-6666-6666-6666-666666666666");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		jdbcTemplate.update("DELETE FROM sale_operations");
		jdbcTemplate.update("DELETE FROM sale_payments");
		jdbcTemplate.update("DELETE FROM sale_items");
		jdbcTemplate.update("DELETE FROM sales");
		jdbcTemplate.update("DELETE FROM sale_receipt_sequences");
		jdbcTemplate.update("DELETE FROM shift_operations");
		jdbcTemplate.update("DELETE FROM shift_cash_movements");
		jdbcTemplate.update("DELETE FROM cashier_shifts");
		jdbcTemplate.update("DELETE FROM inventory_operations");
		jdbcTemplate.update("DELETE FROM inventory_movements");
		jdbcTemplate.update("DELETE FROM inventory_balances");
		jdbcTemplate.update("DELETE FROM auth_sessions");
		jdbcTemplate.update("DELETE FROM product_barcodes");
		jdbcTemplate.update("DELETE FROM products");
		jdbcTemplate.update("DELETE FROM pos_devices");
		jdbcTemplate.update("DELETE FROM membership_branches");
		jdbcTemplate.update("DELETE FROM branches");
		jdbcTemplate.update("DELETE FROM tenant_memberships");
		jdbcTemplate.update("DELETE FROM users");
		jdbcTemplate.update("DELETE FROM tenants");

		jdbcTemplate.update(
			"INSERT INTO tenants (id, name, default_currency, display_timezone) VALUES (?, ?, 'USD', 'Africa/Harare')",
			TENANT_A, "Tenant A");
		jdbcTemplate.update(
			"INSERT INTO tenants (id, name, default_currency, display_timezone) VALUES (?, ?, 'USD', 'Africa/Harare')",
			TENANT_B, "Tenant B");
		jdbcTemplate.update(
			"INSERT INTO users (id, email, display_name, password_hash, status) VALUES (?, ?, ?, ?, 'ACTIVE')",
			USER_ID, "owner@example.com", "Owner A", passwordEncoder.encode("secret123"));
		jdbcTemplate.update(
			"INSERT INTO users (id, email, display_name, password_hash, status) VALUES (?, ?, ?, ?, 'DISABLED')",
			USER_DISABLED, "disabled@example.com", "Disabled", passwordEncoder.encode("secret123"));
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_A, USER_ID);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_B, USER_ID);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_A, USER_DISABLED);
		jdbcTemplate.update(
			"INSERT INTO branches (tenant_id, id, code, name, active) VALUES (?, ?, 'HRE01', 'Harare', true)",
			TENANT_A, BRANCH_A);
		jdbcTemplate.update(
			"INSERT INTO branches (tenant_id, id, code, name, active) VALUES (?, ?, 'BYO01', 'Bulawayo', true)",
			TENANT_B, BRANCH_B);
		jdbcTemplate.update(
			"""
				INSERT INTO pos_devices (tenant_id, id, branch_id, name, platform, status)
				VALUES (?, ?, ?, 'Till A', 'WINDOWS', 'ACTIVE')
				""",
			TENANT_A, DEVICE_A, BRANCH_A);
		jdbcTemplate.update(
			"""
				INSERT INTO pos_devices (tenant_id, id, branch_id, name, platform, status)
				VALUES (?, ?, ?, 'Till B', 'WINDOWS', 'ACTIVE')
				""",
			TENANT_B, DEVICE_B, BRANCH_B);
	}

	@Test
	void loginMeRefreshLogoutAndAccessBranches() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"owner@example.com","password":"secret123"}
					"""))
			.andExpect(status().isOk())
			.andExpect(header().string("Set-Cookie", containsString("shopos_refresh=")))
			.andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
			.andExpect(header().string("Set-Cookie", containsString("Path=/api/v1/auth")))
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty())
			.andExpect(jsonPath("$.user.email").value("owner@example.com"))
			.andExpect(jsonPath("$.user.memberships[0].tenantId").value(TENANT_A.toString()))
			.andReturn();

		JsonNode body = objectMapper.readTree(login.getResponse().getContentAsString());
		String accessToken = body.get("accessToken").asText();
		String refreshToken = body.get("refreshToken").asText();

		mockMvc.perform(get("/api/v1/me")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(USER_ID.toString()))
			.andExpect(jsonPath("$.memberships.length()").value(2));

		mockMvc.perform(get("/api/v1/branches")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].code").value("HRE01"));

		MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
			.andExpect(status().isOk())
			.andExpect(header().string("Set-Cookie", containsString("shopos_refresh=")))
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty())
			.andReturn();

		String newRefresh = objectMapper.readTree(refreshed.getResponse().getContentAsString()).get("refreshToken").asText();

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"" + newRefresh + "\"}"))
			.andExpect(status().isNoContent())
			.andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"refreshToken\":\"" + newRefresh + "\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshAndLogoutViaHttpOnlyCookie() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"owner@example.com","password":"secret123"}
					"""))
			.andExpect(status().isOk())
			.andReturn();

		String refreshToken = objectMapper.readTree(login.getResponse().getContentAsString())
			.get("refreshToken")
			.asText();
		Cookie cookie = new Cookie("shopos_refresh", refreshToken);

		MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh").cookie(cookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty())
			.andReturn();

		String rotated = objectMapper.readTree(refreshed.getResponse().getContentAsString())
			.get("refreshToken")
			.asText();

		mockMvc.perform(post("/api/v1/auth/logout").cookie(new Cookie("shopos_refresh", rotated)))
			.andExpect(status().isNoContent())
			.andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

		mockMvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("shopos_refresh", rotated)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void loginWithDeviceIdSelectsDeviceTenant() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email":"owner@example.com",
					  "password":"secret123",
					  "deviceId":"%s"
					}
					""".formatted(DEVICE_B)))
			.andExpect(status().isOk())
			.andReturn();

		String accessToken = objectMapper.readTree(login.getResponse().getContentAsString())
			.get("accessToken")
			.asText();

		// JWT tid follows the device's tenant (B); Bearer access lists only that tenant's branches.
		mockMvc.perform(get("/api/v1/branches")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].code").value("BYO01"));
	}

	@Test
	void badPasswordUnknownEmailAndDisabledUserReturnUnauthorized() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"owner@example.com","password":"wrong"}
					"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"missing@example.com","password":"secret123"}
					"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"disabled@example.com","password":"secret123"}
					"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void meWithoutBearerIsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}
}
