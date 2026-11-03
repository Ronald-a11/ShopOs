package zw.miccs.shopos.shifts;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import zw.miccs.shopos.TestcontainersConfiguration;
import zw.miccs.shopos.identity.infrastructure.DevAuthenticationFilter;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class ShiftApiIntegrationTest {

	private static final UUID TENANT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID TENANT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	private static final UUID USER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID USER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID USER_CASHIER = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID USER_SUPERVISOR = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private static final UUID USER_STOCK = UUID.fromString("55555555-5555-5555-5555-555555555555");
	private static final UUID BRANCH_A = UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa");
	private static final UUID BRANCH_B = UUID.fromString("bbbbbbbb-1111-1111-1111-bbbbbbbbbbbb");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private UUID deviceId;

	@BeforeEach
	void setUp() throws Exception {
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
			"INSERT INTO users (id, email, display_name, status) VALUES (?, ?, ?, 'ACTIVE')",
			USER_CASHIER, "cashier-a@example.com", "Cashier A");
		jdbcTemplate.update(
			"INSERT INTO users (id, email, display_name, status) VALUES (?, ?, ?, 'ACTIVE')",
			USER_SUPERVISOR, "supervisor-a@example.com", "Supervisor A");
		jdbcTemplate.update(
			"INSERT INTO users (id, email, display_name, status) VALUES (?, ?, ?, 'ACTIVE')",
			USER_STOCK, "stock-a@example.com", "Stock A");
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_A, USER_A);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_B, USER_B);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'CASHIER')",
			TENANT_A, USER_CASHIER);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'SUPERVISOR')",
			TENANT_A, USER_SUPERVISOR);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'STOCK_CONTROLLER')",
			TENANT_A, USER_STOCK);
		jdbcTemplate.update(
			"INSERT INTO branches (tenant_id, id, code, name, active) VALUES (?, ?, 'HRE01', 'Harare', true)",
			TENANT_A, BRANCH_A);
		jdbcTemplate.update(
			"INSERT INTO branches (tenant_id, id, code, name, active) VALUES (?, ?, 'BYO01', 'Bulawayo', true)",
			TENANT_B, BRANCH_B);
		jdbcTemplate.update(
			"INSERT INTO membership_branches (tenant_id, user_id, branch_id) VALUES (?, ?, ?)",
			TENANT_A, USER_CASHIER, BRANCH_A);
		jdbcTemplate.update(
			"INSERT INTO membership_branches (tenant_id, user_id, branch_id) VALUES (?, ?, ?)",
			TENANT_A, USER_SUPERVISOR, BRANCH_A);
		jdbcTemplate.update(
			"INSERT INTO membership_branches (tenant_id, user_id, branch_id) VALUES (?, ?, ?)",
			TENANT_A, USER_STOCK, BRANCH_A);

		MvcResult device = mockMvc.perform(post("/api/v1/branches/{branchId}/devices", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Till 1","platform":"WINDOWS"}
					"""))
			.andExpect(status().isCreated())
			.andReturn();
		deviceId = UUID.fromString(objectMapper.readTree(device.getResponse().getContentAsString()).get("id").asText());
	}

	@Test
	void openCashMovementAndCloseWithExpectedMath() throws Exception {
		UUID openOp = UUID.randomUUID();
		MvcResult opened = mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "deviceId":"%s",
					  "openingFloat":"100.00",
					  "currency":"USD",
					  "clientOperationId":"%s"
					}
					""".formatted(BRANCH_A, deviceId, openOp)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("OPEN"))
			.andExpect(jsonPath("$.openingFloat").value("100"))
			.andExpect(jsonPath("$.expectedCash").value("100"))
			.andExpect(jsonPath("$.cashierUserId").value(USER_CASHIER.toString()))
			.andReturn();
		UUID shiftId = UUID.fromString(objectMapper.readTree(opened.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(post("/api/v1/shifts/{id}/cash-movements", shiftId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"type":"PAID_IN","amount":"20.00","currency":"USD","reason":"Change bag"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.type").value("PAID_IN"))
			.andExpect(jsonPath("$.amount").value("20"));

		mockMvc.perform(post("/api/v1/shifts/{id}/cash-movements", shiftId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"type":"DROP","amount":"30.00","currency":"USD"}
					"""))
			.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/shifts/{id}/close", shiftId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"countedCash":[{"currency":"USD","amount":"90.00"}]}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CLOSED"))
			.andExpect(jsonPath("$.expectedCash").value("90"))
			.andExpect(jsonPath("$.countedCash").value("90"))
			.andExpect(jsonPath("$.variance").value("0"));
	}

	@Test
	void replayOpenClientOperationIdDoesNotCreateSecondShift() throws Exception {
		UUID opId = UUID.randomUUID();
		String body = """
			{
			  "branchId":"%s",
			  "deviceId":"%s",
			  "openingFloat":"50",
			  "currency":"USD",
			  "clientOperationId":"%s"
			}
			""".formatted(BRANCH_A, deviceId, opId);

		MvcResult first = mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andReturn();
		String shiftId = objectMapper.readTree(first.getResponse().getContentAsString()).get("id").asText();

		mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(shiftId));
	}

	@Test
	void secondOpenWithoutCloseConflicts() throws Exception {
		openCashierShift(UUID.randomUUID(), "10");

		mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "deviceId":"%s",
					  "openingFloat":"10",
					  "currency":"USD",
					  "clientOperationId":"%s"
					}
					""".formatted(BRANCH_A, deviceId, UUID.randomUUID())))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SHIFT_ALREADY_OPEN"));
	}

	@Test
	void varianceRequiresSupervisorApproval() throws Exception {
		MvcResult opened = openCashierShift(UUID.randomUUID(), "100");
		UUID shiftId = UUID.fromString(objectMapper.readTree(opened.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(post("/api/v1/shifts/{id}/close", shiftId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"countedCash":[{"currency":"USD","amount":"95.00"}]}
					"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SHIFT_VARIANCE_APPROVAL_REQUIRED"));

		mockMvc.perform(post("/api/v1/shifts/{id}/close", shiftId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_SUPERVISOR)
				.header(DevAuthenticationFilter.ROLE_HEADER, "SUPERVISOR")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "countedCash":[{"currency":"USD","amount":"95.00"}],
					  "approvalReason":"Short explained"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CLOSED"))
			.andExpect(jsonPath("$.variance").value("-5"));
	}

	@Test
	void crossTenantBranchIsNotFound() throws Exception {
		mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_B)
				.header(DevAuthenticationFilter.USER_HEADER, USER_B)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "deviceId":"%s",
					  "openingFloat":"10",
					  "currency":"USD"
					}
					""".formatted(BRANCH_A, deviceId)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));
	}

	@Test
	void unknownDeviceIsNotFound() throws Exception {
		mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "deviceId":"%s",
					  "openingFloat":"10",
					  "currency":"USD"
					}
					""".formatted(BRANCH_A, UUID.randomUUID())))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("DEVICE_NOT_FOUND"));
	}

	@Test
	void stockControllerCannotOpenShift() throws Exception {
		mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_STOCK)
				.header(DevAuthenticationFilter.ROLE_HEADER, "STOCK_CONTROLLER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "deviceId":"%s",
					  "openingFloat":"10",
					  "currency":"USD"
					}
					""".formatted(BRANCH_A, deviceId)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	private MvcResult openCashierShift(UUID clientOperationId, String openingFloat) throws Exception {
		return mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "deviceId":"%s",
					  "openingFloat":"%s",
					  "currency":"USD",
					  "clientOperationId":"%s"
					}
					""".formatted(BRANCH_A, deviceId, openingFloat, clientOperationId)))
			.andExpect(status().isCreated())
			.andReturn();
	}
}
