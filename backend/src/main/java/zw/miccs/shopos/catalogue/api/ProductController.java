package zw.miccs.shopos.catalogue.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import zw.miccs.shopos.catalogue.api.CatalogueDtos.CreateProductRequest;
import zw.miccs.shopos.catalogue.api.CatalogueDtos.ProductResponse;
import zw.miccs.shopos.catalogue.application.CatalogueService;
import zw.miccs.shopos.catalogue.application.ProductView;
import zw.miccs.shopos.common.AuthenticationSupport;
import zw.miccs.shopos.identity.application.AuthenticatedPrincipal;
import zw.miccs.shopos.tenancy.application.CurrentTenantService;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catalogue")
public class ProductController {

	private final CatalogueService catalogue;
	private final CurrentTenantService tenants;

	public ProductController(CatalogueService catalogue, CurrentTenantService tenants) {
		this.catalogue = catalogue;
		this.tenants = tenants;
	}

	@GetMapping("/products")
	@Operation(summary = "Search active products by text or barcode")
	ResponseEntity<List<ProductResponse>> search(
		Authentication authentication,
		@RequestParam(required = false) String q,
		@RequestParam(required = false) String barcode
	) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(catalogue.searchProducts(tenantId, q, barcode).stream()
			.map(ProductController::toResponse)
			.toList());
	}

	@GetMapping("/products/{productId}")
	@Operation(summary = "Get a product")
	ResponseEntity<ProductResponse> get(Authentication authentication, @PathVariable UUID productId) {
		UUID tenantId = tenantId(authentication);
		return ResponseEntity.ok(toResponse(catalogue.requireProduct(tenantId, productId)));
	}

	@PostMapping("/products")
	@Operation(summary = "Create a product")
	ResponseEntity<ProductResponse> create(
		Authentication authentication,
		@Valid @RequestBody CreateProductRequest request
	) {
		UUID tenantId = tenantId(authentication);
		ProductView created = catalogue.create(
			tenantId,
			request.sku(),
			request.name(),
			request.barcode(),
			request.unitPrice(),
			request.taxRateCode());
		return ResponseEntity.status(201).body(toResponse(created));
	}

	private UUID tenantId(Authentication authentication) {
		AuthenticatedPrincipal principal = AuthenticationSupport.requirePrincipal(authentication);
		return tenants.requirePrimaryTenantId(principal.userId());
	}

	private static ProductResponse toResponse(ProductView view) {
		return new ProductResponse(
			view.id(),
			view.sku(),
			view.name(),
			view.barcode(),
			view.unitPrice(),
			view.taxRateCode(),
			view.status());
	}

}
