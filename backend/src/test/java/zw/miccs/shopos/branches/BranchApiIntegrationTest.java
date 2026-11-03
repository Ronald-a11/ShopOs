package zw.miccs.shopos.branches;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import zw.miccs.shopos.TestcontainersConfiguration;
import zw.miccs.shopos.identity.infrastructure.DevAuthenticationFilter;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class BranchApiIntegrationTest {

	private static final UUID TENANT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID TENANT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	private static final UUID USER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID USER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

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
			"INSERT INTO users (id, email, display_name, status) VALUES (?, ?, ?, 'ACTIVE')",
			USER_A, "owner-a@example.com", "Owner A");
		jdbcTemplate.update(
			"INSERT INTO users (id, email, display_name, status) VALUES (?, ?, ?, 'ACTIVE')",
			USER_B, "owner-b@example.com", "Owner B");
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_A, USER_A);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_B, USER_B);
	}

	@Test
	void createListGetPatchAndRegisterDevice() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/branches")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"HRE01","name":"Harare CBD","address":"1 Samora Machel"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.code").value("HRE01"))
			.andExpect(jsonPath("$.active").value(true))
			.andReturn();

		UUID branchId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(get("/api/v1/branches")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.page.totalElements").value(1));

		mockMvc.perform(get("/api/v1/branches/{id}", branchId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Harare CBD"));

		mockMvc.perform(patch("/api/v1/branches/{id}", branchId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Harare Centre","active":true}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Harare Centre"));

		mockMvc.perform(post("/api/v1/branches/{id}/devices", branchId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Till 1","platform":"WINDOWS","externalSerial":"SN-1"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.platform").value("WINDOWS"))
			.andExpect(jsonPath("$.status").value("ACTIVE"))
			.andExpect(jsonPath("$.branchId").value(branchId.toString()));

		mockMvc.perform(get("/api/v1/branches/{id}/devices", branchId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void otherTenantCannotReadBranchById() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/branches")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"HRE02","name":"Secret Branch"}
					"""))
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
		UUID branchId = UUID.fromString(body.get("id").asText());

		mockMvc.perform(get("/api/v1/branches/{id}", branchId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_B)
				.header(DevAuthenticationFilter.USER_HEADER, USER_B)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));

		assertThat(branchId).isNotNull();
	}

	@Test
	void cashierCannotCreateBranch() throws Exception {
		mockMvc.perform(post("/api/v1/branches")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"code":"X","name":"Nope"}
					"""))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}
}
