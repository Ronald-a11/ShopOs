package zw.miccs.shopos.sales;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class SalesApiIntegrationTest {

	private static final UUID TENANT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID TENANT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	private static final UUID USER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID USER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID USER_CASHIER = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID USER_SUPERVISOR = UUID.fromString("44444444-4444-4444-4444-444444444444");
	private static final UUID BRANCH_A = UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa");
	private static final UUID BRANCH_B = UUID.fromString("bbbbbbbb-1111-1111-1111-bbbbbbbbbbbb");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private UUID deviceId;
	private UUID productId;

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

		MvcResult product = mockMvc.perform(post("/api/v1/products")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"sku":"SKU-1","name":"Maize Meal 2kg","taxCode":"VAT15","unitPrice":"2.00","currency":"USD"}
					"""))
			.andExpect(status().isCreated())
			.andReturn();
		productId = UUID.fromString(objectMapper.readTree(product.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(post("/api/v1/inventory/receipts")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "clientOperationId":"%s",
					  "lines":[{"productId":"%s","quantity":"100"}]
					}
					""".formatted(BRANCH_A, UUID.randomUUID(), productId)))
			.andExpect(status().isCreated());

		openShift(USER_CASHIER, "CASHIER", "50.00");
	}

	@Test
	void commitSaleUpdatesInventorySearchAndShiftCash() throws Exception {
		UUID clientSaleId = UUID.randomUUID();
		// 2 * 2.00 = 4.00 net; tax 0.60; grand 4.60; pay 5.00 cash → change 0.40
		MvcResult committed = mockMvc.perform(post("/api/v1/sales")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(saleBody(clientSaleId, "2", "5.00")))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.replayed").value(false))
			.andExpect(jsonPath("$.receiptNumber").value("HRE01-000001"))
			.andExpect(jsonPath("$.totals.grandTotal").value("4.6"))
			.andExpect(jsonPath("$.change").value("0.4"))
			.andExpect(jsonPath("$.inventory.status").value("APPLIED"))
			.andExpect(jsonPath("$.fiscal.status").value("PENDING_SUBMISSION"))
			.andReturn();

		UUID saleId = UUID.fromString(
			objectMapper.readTree(committed.getResponse().getContentAsString()).get("saleId").asText());

		mockMvc.perform(get("/api/v1/sales/{id}", saleId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("COMMITTED"))
			.andExpect(jsonPath("$.items.length()").value(1));

		mockMvc.perform(get("/api/v1/sales")
				.param("branchId", BRANCH_A.toString())
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1));

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].quantity").value("98"));

		// opening 50 + cash tender 5 = 55
		org.junit.jupiter.api.Assertions.assertEquals(0, new java.math.BigDecimal("55").compareTo(
			jdbcTemplate.queryForObject(
				"SELECT expected_cash FROM cashier_shifts WHERE tenant_id = ? AND cashier_user_id = ? AND status = 'OPEN'",
				java.math.BigDecimal.class,
				TENANT_A,
				USER_CASHIER)));
	}

	@Test
	void replaySameClientSaleIdDoesNotDoubleStock() throws Exception {
		UUID clientSaleId = UUID.randomUUID();
		String body = saleBody(clientSaleId, "1", "3.00");

		MvcResult first = mockMvc.perform(post("/api/v1/sales")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.replayed").value(false))
			.andReturn();
		String saleId = objectMapper.readTree(first.getResponse().getContentAsString()).get("saleId").asText();

		mockMvc.perform(post("/api/v1/sales")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.replayed").value(true))
			.andExpect(jsonPath("$.saleId").value(saleId));

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].quantity").value("99"));
	}

	@Test
	void returnRestoresStockAndMarksOriginal() throws Exception {
		UUID saleId = commitSale(UUID.randomUUID(), "4", "10.00");
		openShift(USER_SUPERVISOR, "SUPERVISOR", "0");

		mockMvc.perform(post("/api/v1/sales/{id}/returns", saleId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_SUPERVISOR)
				.header(DevAuthenticationFilter.ROLE_HEADER, "SUPERVISOR")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "clientOperationId":"%s",
					  "reason":"Customer return",
					  "lines":[{"productId":"%s","quantity":"2"}]
					}
					""".formatted(UUID.randomUUID(), productId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("RETURNED"));

		mockMvc.perform(get("/api/v1/sales/{id}", saleId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_SUPERVISOR)
				.header(DevAuthenticationFilter.ROLE_HEADER, "SUPERVISOR")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("PARTIALLY_RETURNED"));

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].quantity").value("98"));
	}

	@Test
	void voidRestoresStockAndBlocksSecondVoid() throws Exception {
		UUID saleId = commitSale(UUID.randomUUID(), "3", "10.00");

		mockMvc.perform(post("/api/v1/sales/{id}/void-request", saleId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_SUPERVISOR)
				.header(DevAuthenticationFilter.ROLE_HEADER, "SUPERVISOR")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"clientOperationId":"%s","reason":"Wrong till"}
					""".formatted(UUID.randomUUID())))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("VOIDED"));

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].quantity").value("100"));

		mockMvc.perform(post("/api/v1/sales/{id}/void-request", saleId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_SUPERVISOR)
				.header(DevAuthenticationFilter.ROLE_HEADER, "SUPERVISOR")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"clientOperationId":"%s","reason":"Again"}
					""".formatted(UUID.randomUUID())))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("SALE_NOT_VOIDABLE"));
	}

	@Test
	void cashierCannotVoid() throws Exception {
		UUID saleId = commitSale(UUID.randomUUID(), "1", "3.00");

		mockMvc.perform(post("/api/v1/sales/{id}/void-request", saleId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"clientOperationId":"%s","reason":"Oops"}
					""".formatted(UUID.randomUUID())))
			.andExpect(status().isForbidden());
	}

	@Test
	void crossTenantGetIsNotFound() throws Exception {
		UUID saleId = commitSale(UUID.randomUUID(), "1", "3.00");

		mockMvc.perform(get("/api/v1/sales/{id}", saleId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_B)
				.header(DevAuthenticationFilter.USER_HEADER, USER_B)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("SALE_NOT_FOUND"));
	}

	@Test
	void paymentShortIsUnprocessable() throws Exception {
		mockMvc.perform(post("/api/v1/sales")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(saleBody(UUID.randomUUID(), "2", "1.00")))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.code").value("PAYMENT_SHORT"));
	}

	private UUID commitSale(UUID clientSaleId, String qty, String pay) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/sales")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(saleBody(clientSaleId, qty, pay)))
			.andExpect(status().isCreated())
			.andReturn();
		return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("saleId").asText());
	}

	private String saleBody(UUID clientSaleId, String qty, String payAmount) {
		return """
			{
			  "clientSaleId":"%s",
			  "branchId":"%s",
			  "deviceId":"%s",
			  "occurredAt":"2026-11-03T08:00:00Z",
			  "configVersion":1,
			  "currency":"USD",
			  "items":[{"productId":"%s","quantity":"%s"}],
			  "payments":[{"method":"CASH","currency":"USD","amount":"%s"}]
			}
			""".formatted(clientSaleId, BRANCH_A, deviceId, productId, qty, payAmount);
	}

	private void openShift(UUID userId, String role, String openingFloat) throws Exception {
		mockMvc.perform(post("/api/v1/shifts/open")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, userId)
				.header(DevAuthenticationFilter.ROLE_HEADER, role)
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
					""".formatted(BRANCH_A, deviceId, openingFloat, UUID.randomUUID())))
			.andExpect(status().isCreated());
	}
}
