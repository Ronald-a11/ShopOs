package zw.miccs.shopos.catalogue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
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
class CatalogueApiIntegrationTest {

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
	void createSearchGetPatchBarcodeLookupAndImport() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/products")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "sku":"SKU-1",
					  "name":"Maize Meal 2kg",
					  "description":"Staple",
					  "taxCode":"VAT15",
					  "unitPrice":"2.50",
					  "currency":"USD",
					  "barcodes":[{"barcode":"6001234","unitMultiplier":"1"}]
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.sku").value("SKU-1"))
			.andExpect(jsonPath("$.unitPrice").value("2.5"))
			.andExpect(jsonPath("$.barcodes[0].barcode").value("6001234"))
			.andReturn();

		UUID productId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(get("/api/v1/products")
				.param("q", "Maize")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(1));

		mockMvc.perform(get("/api/v1/products/{id}", productId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Maize Meal 2kg"));

		mockMvc.perform(patch("/api/v1/products/{id}", productId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name":"Maize Meal 5kg",
					  "unitPrice":"5.00",
					  "barcodes":[{"barcode":"6009999","unitMultiplier":"1"}]
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Maize Meal 5kg"))
			.andExpect(jsonPath("$.unitPrice").value("5"))
			.andExpect(jsonPath("$.barcodes[0].barcode").value("6009999"));

		mockMvc.perform(get("/api/v1/products/barcode/{barcode}", "6009999")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.product.id").value(productId.toString()))
			.andExpect(jsonPath("$.unitMultiplier").value("1"));

		String csv = """
			sku,name,description,taxCode,unitPrice,currency,barcode,unitMultiplier
			SKU-1,Maize Meal Updated,Staple,VAT15,5.50,USD,6009999,1
			SKU-2,Cooking Oil,Oil,VAT15,3.00,USD,7001111,1
			SKU-BAD,,,,VAT15,x,USD,,
			""";
		MockMultipartFile file = new MockMultipartFile(
			"file",
			"products.csv",
			"text/csv",
			csv.getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(multipart("/api/v1/products/import")
				.file(file)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.created").value(1))
			.andExpect(jsonPath("$.updated").value(1))
			.andExpect(jsonPath("$.errors.length()").value(1));
	}

	@Test
	void otherTenantCannotReadProductById() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/products")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"sku":"SECRET","name":"Secret","taxCode":"VAT15"}
					"""))
			.andExpect(status().isCreated())
			.andReturn();

		UUID productId = UUID.fromString(objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText());

		mockMvc.perform(get("/api/v1/products/{id}", productId)
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_B)
				.header(DevAuthenticationFilter.USER_HEADER, USER_B)
				.header(DevAuthenticationFilter.ROLE_HEADER, "OWNER"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
	}

	@Test
	void cashierCannotCreateProduct() throws Exception {
		mockMvc.perform(post("/api/v1/products")
				.header(DevAuthenticationFilter.TENANT_HEADER, TENANT_A)
				.header(DevAuthenticationFilter.USER_HEADER, USER_A)
				.header(DevAuthenticationFilter.ROLE_HEADER, "CASHIER")
				.header(DevAuthenticationFilter.BRANCHES_HEADER, UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"sku":"X","name":"Nope","taxCode":"VAT15"}
					"""))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}
}
