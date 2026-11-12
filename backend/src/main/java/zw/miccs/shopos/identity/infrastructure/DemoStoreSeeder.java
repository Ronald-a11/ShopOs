package zw.miccs.shopos.identity.infrastructure;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.application.BranchService;
import zw.miccs.shopos.branches.application.BranchView;
import zw.miccs.shopos.branches.application.DeviceView;
import zw.miccs.shopos.catalogue.application.CatalogueService;
import zw.miccs.shopos.catalogue.application.ProductView;
import zw.miccs.shopos.inventory.application.BalanceView;
import zw.miccs.shopos.inventory.application.InventoryService;
import zw.miccs.shopos.inventory.application.StockLine;
import zw.miccs.shopos.tenancy.application.TenantQueryService;

/**
 * Seeds the Demo Grocers store when {@code shopos.auth.seed-demo-user=true}.
 * Safe to run repeatedly: existing branch, device, product, and balance rows are left in place.
 */
@Component
@Order(100)
public class DemoStoreSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoStoreSeeder.class);
	private static final String TENANT_NAME = "Demo Grocers";

	private final boolean enabled;
	private final TenantQueryService tenants;
	private final BranchService branches;
	private final CatalogueService catalogue;
	private final InventoryService inventory;

	public DemoStoreSeeder(
		@Value("${shopos.auth.seed-demo-user:false}") boolean enabled,
		TenantQueryService tenants,
		BranchService branches,
		CatalogueService catalogue,
		InventoryService inventory
	) {
		this.enabled = enabled;
		this.tenants = tenants;
		this.branches = branches;
		this.catalogue = catalogue;
		this.inventory = inventory;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!enabled) {
			return;
		}
		Optional<UUID> tenantId = tenants.findActiveTenantIdByName(TENANT_NAME);
		if (tenantId.isEmpty()) {
			log.info("Skipped demo store seed; tenant '{}' is not present", TENANT_NAME);
			return;
		}
		UUID tenant = tenantId.get();
		BranchView harare = ensureBranch(tenant, "BR-001", "Harare CBD", "Samora Machel Ave, Harare");
		ensureBranch(tenant, "BR-002", "Borrowdale", "Borrowdale Road, Harare");
		ensureBranch(tenant, "BR-003", "Bulawayo Main", "Fife Street, Bulawayo");
		ensureBranch(tenant, "BR-004", "Mutare", "Herbert Chitepo St, Mutare");
		ensureBranch(tenant, "BR-005", "Gweru", "Main Street, Gweru");

		ensureDevice(tenant, harare.id(), "T01", "Counter till 01", "DESKTOP", "AVAILABLE");
		ensureDevice(tenant, harare.id(), "T02", "Counter till 02", "DESKTOP", "IN_USE");
		ensureDevice(tenant, harare.id(), "HH01", "Handheld", "ANDROID", "AVAILABLE");
		ensureDevice(tenant, harare.id(), "K01", "Self-service kiosk", "KIOSK", "OFFLINE");

		List<SeedProduct> definitions = List.of(
			new SeedProduct("MEAL-2KG", "Mealie Meal 2kg", "6001234000011", new BigDecimal("4.50"), "VAT15", new BigDecimal("48")),
			new SeedProduct("COOK-2L", "Cooking Oil 2L", "6001234000028", new BigDecimal("6.20"), "VAT15", new BigDecimal("36")),
			new SeedProduct("SUG-1KG", "Sugar 1kg", "6001234000035", new BigDecimal("1.80"), "VAT15", new BigDecimal("80")),
			new SeedProduct("BREAD-W", "White Bread", "6001234000042", new BigDecimal("1.20"), "VAT0", new BigDecimal("20")),
			new SeedProduct("SOAP-BAR", "Laundry Soap", "6001234000059", new BigDecimal("1.50"), "VAT15", new BigDecimal("40"))
		);

		List<StockLine> opening = new ArrayList<>();
		Set<UUID> stocked = inventory.listBalances(tenant, harare.id()).stream()
			.map(BalanceView::productId)
			.collect(Collectors.toSet());
		for (SeedProduct definition : definitions) {
			ProductView product = catalogue.findBySku(tenant, definition.sku())
				.orElseGet(() -> catalogue.create(
					tenant,
					definition.sku(),
					definition.name(),
					definition.barcode(),
					definition.unitPrice(),
					definition.taxRateCode()));
			if (!stocked.contains(product.id())) {
				opening.add(new StockLine(product.id(), definition.quantity()));
			}
		}
		if (!opening.isEmpty()) {
			inventory.receive(tenant, harare.id(), opening, "Opening stock");
		}
		log.info("Demo store '{}' is ready ({} products, Harare CBD stock seeded when missing)", TENANT_NAME, definitions.size());
	}

	private BranchView ensureBranch(UUID tenantId, String code, String name, String address) {
		return branches.listBranches(tenantId).stream()
			.filter(branch -> code.equals(branch.code()))
			.findFirst()
			.orElseGet(() -> branches.create(tenantId, code, name, address));
	}

	private void ensureDevice(UUID tenantId, UUID branchId, String code, String name, String platform, String status) {
		boolean exists = branches.listDevices(tenantId, branchId).stream()
			.anyMatch(device -> code.equals(device.code()));
		if (!exists) {
			DeviceView created = branches.registerDevice(tenantId, branchId, code, name, platform, status);
			log.debug("Seeded device {} ({})", created.code(), created.status());
		}
	}

	private record SeedProduct(
		String sku,
		String name,
		String barcode,
		BigDecimal unitPrice,
		String taxRateCode,
		BigDecimal quantity
	) {
	}

}
