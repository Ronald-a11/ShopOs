package zw.miccs.shopos.inventory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
class InventoryApiIntegrationTest {

	private static final UUID TENANT_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final UUID TENANT_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
	private static final UUID USER_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID USER_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID USER_CASHIER = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID BRANCH_A = UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa");
	private static final UUID BRANCH_B = UUID.fromString("bbbbbbbb-1111-1111-1111-bbbbbbbbbbbb");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

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
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_A, USER_A);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'OWNER')",
			TENANT_B, USER_B);
		jdbcTemplate.update(
			"INSERT INTO tenant_memberships (tenant_id, user_id, role) VALUES (?, ?, 'CASHIER')",
			TENANT_A, USER_CASHIER);
		jdbcTemplate.update(
			"INSERT INTO branches (tenant_id, id, code, name, active) VALUES (?, ?, 'HRE01', 'Harare', true)",
			TENANT_A, BRANCH_A);
		jdbcTemplate.update(
			"INSERT INTO branches (tenant_id, id, code, name, active) VALUES (?, ?, 'BYO01', 'Bulawayo', true)",
			TENANT_B, BRANCH_B);
		jdbcTemplate.update(
			"INSERT INTO membership_branches (tenant_id, user_id, branch_id) VALUES (?, ?, ?)",
			TENANT_A, USER_CASHIER, BRANCH_A);

		MvcResult created = mockMvc.perform(post("/api/v1/products")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"sku":"SKU-1","name":"Maize Meal 2kg","taxCode":"VAT15","unitPrice":"2.50","currency":"USD"}
					"""))
			.andExpect(status().isCreated())
			.andReturn();
		productId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());
	}

	@Test
	void receiveGoodsUpdatesBalancesAndMovements() throws Exception {
		UUID opId = UUID.randomUUID();
		mockMvc.perform(post("/api/v1/inventory/receipts")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "clientOperationId":"%s",
					  "reason":"PO-1",
					  "lines":[{"productId":"%s","quantity":"10"}]
					}
					""".formatted(BRANCH_A, opId, productId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.branchId").value(BRANCH_A.toString()))
			.andExpect(jsonPath("$.movements.length()").value(1))
			.andExpect(jsonPath("$.movements[0].movementType").value("RECEIPT"))
			.andExpect(jsonPath("$.movements[0].quantityDelta").value("10"));

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].productId").value(productId.toString()))
			.andExpect(jsonPath("$.content[0].sku").value("SKU-1"))
			.andExpect(jsonPath("$.content[0].quantity").value("10"));

		mockMvc.perform(get("/api/v1/inventory/movements")
				.param("branchId", BRANCH_A.toString())
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].movementType").value("RECEIPT"));
	}

	@Test
	void replaySameClientOperationIdDoesNotDoubleStock() throws Exception {
		UUID opId = UUID.randomUUID();
		String body = """
			{
			  "branchId":"%s",
			  "clientOperationId":"%s",
			  "lines":[{"productId":"%s","quantity":"5"}]
			}
			""".formatted(BRANCH_A, opId, productId);

		MvcResult first = mockMvc.perform(post("/api/v1/inventory/receipts")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andReturn();
		String receiptId = objectMapper.readTree(first.getResponse().getContentAsString()).get("receiptId").asText();

		mockMvc.perform(post("/api/v1/inventory/receipts")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.receiptId").value(receiptId))
			.andExpect(jsonPath("$.movements.length()").value(1));

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].quantity").value("5"));
	}

	@Test
	void adjustmentAndWastageChangeQuantity() throws Exception {
		receive(UUID.randomUUID(), "20");

		UUID adjustOp = UUID.randomUUID();
		mockMvc.perform(post("/api/v1/inventory/adjustments")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "clientOperationId":"%s",
					  "reason":"Cycle count",
					  "movementType":"ADJUSTMENT",
					  "lines":[{"productId":"%s","quantityDelta":"3"}]
					}
					""".formatted(BRANCH_A, adjustOp, productId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.movements[0].movementType").value("ADJUSTMENT"))
			.andExpect(jsonPath("$.movements[0].quantityDelta").value("3"));

		UUID wastageOp = UUID.randomUUID();
		mockMvc.perform(post("/api/v1/inventory/adjustments")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "clientOperationId":"%s",
					  "reason":"Damaged",
					  "movementType":"WASTAGE",
					  "lines":[{"productId":"%s","quantityDelta":"-2"}]
					}
					""".formatted(BRANCH_A, wastageOp, productId)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.movements[0].movementType").value("WASTAGE"))
			.andExpect(jsonPath("$.movements[0].quantityDelta").value("-2"));

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].quantity").value("21"));
	}

	@Test
	void lowStockReturnsOnlyReorderBreaches() throws Exception {
		receive(UUID.randomUUID(), "4");
		jdbcTemplate.update(
			"UPDATE inventory_balances SET reorder_level = ? WHERE tenant_id = ? AND branch_id = ? AND product_id = ?",
			new BigDecimal("5"), TENANT_A, BRANCH_A, productId);

		MvcResult otherProduct = mockMvc.perform(post("/api/v1/products")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"sku":"SKU-2","name":"Oil","taxCode":"VAT15"}
					"""))
			.andExpect(status().isCreated())
			.andReturn();
		UUID otherId = UUID.fromString(
			objectMapper.readTree(otherProduct.getResponse().getContentAsString()).get("id").asText());

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
					""".formatted(BRANCH_A, UUID.randomUUID(), otherId)))
			.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/inventory/low-stock")
				.param("branchId", BRANCH_A.toString())
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].productId").value(productId.toString()))
			.andExpect(jsonPath("$.content[0].quantity").value("4"));
	}

	@Test
	void crossTenantBranchAccessIsNotFound() throws Exception {
		receive(UUID.randomUUID(), "1");

		mockMvc.perform(get("/api/v1/branches/{branchId}/inventory", BRANCH_A)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_B)
				.header(DevAuthenticationFilter.USER_HEADER, USER_B)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("BRANCH_NOT_FOUND"));

		mockMvc.perform(get("/api/v1/inventory/movements")
				.param("branchId", BRANCH_A.toString())
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_B)
				.header(DevAuthenticationFilter.USER_HEADER, USER_B)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isNotFound());
	}

	@Test
	void cashierCannotReceiveStock() throws Exception {
		mockMvc.perform(post("/api/v1/inventory/receipts")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_CASHIER)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, BRANCH_A.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "clientOperationId":"%s",
					  "lines":[{"productId":"%s","quantity":"1"}]
					}
					""".formatted(BRANCH_A, UUID.randomUUID(), productId)))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	private void receive(UUID opId, String quantity) throws Exception {
		mockMvc.perform(post("/api/v1/inventory/receipts")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "branchId":"%s",
					  "clientOperationId":"%s",
					  "lines":[{"productId":"%s","quantity":"%s"}]
					}
					""".formatted(BRANCH_A, opId, productId, quantity)))
			.andExpect(status().isCreated());
	}
}
