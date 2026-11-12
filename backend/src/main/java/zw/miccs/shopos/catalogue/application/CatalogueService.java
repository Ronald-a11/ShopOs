package zw.miccs.shopos.catalogue.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.catalogue.domain.ProductStatus;
import zw.miccs.shopos.catalogue.infrastructure.persistence.ProductEntity;
import zw.miccs.shopos.catalogue.infrastructure.persistence.ProductJpaRepository;
import zw.miccs.shopos.common.ApiException;

@Service
public class CatalogueService implements CatalogueQueryService {

	private final ProductJpaRepository products;

	public CatalogueService(ProductJpaRepository products) {
		this.products = products;
	}

	@Override
	@Transactional(readOnly = true)
	public ProductView findActiveProduct(UUID tenantId, UUID productId) {
		ProductEntity entity = products.findByTenantIdAndId(tenantId, productId)
			.orElseThrow(() -> notFound());
		if (!ProductStatus.ACTIVE.name().equals(entity.getStatus())) {
			throw new ApiException(HttpStatus.CONFLICT, "PRODUCT_INACTIVE", "Product inactive",
				"Product is not active");
		}
		return toView(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ProductView> findProduct(UUID tenantId, UUID productId) {
		return products.findByTenantIdAndId(tenantId, productId).map(CatalogueService::toView);
	}

	@Transactional(readOnly = true)
	public ProductView requireProduct(UUID tenantId, UUID productId) {
		return findProduct(tenantId, productId).orElseThrow(CatalogueService::notFound);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ProductView> findByBarcode(UUID tenantId, String barcode) {
		if (barcode == null || barcode.isBlank()) {
			return Optional.empty();
		}
		return products.findFirstByTenantIdAndBarcodeAndStatusOrderByNameAsc(
				tenantId, barcode.trim(), ProductStatus.ACTIVE.name())
			.map(CatalogueService::toView);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProductView> searchProducts(UUID tenantId, String q, String barcode) {
		String normalizedBarcode = blankToNull(barcode);
		String normalizedQuery = blankToNull(q);
		List<ProductEntity> rows;
		if (normalizedBarcode != null) {
			rows = products
				.findFirstByTenantIdAndBarcodeAndStatusOrderByNameAsc(
					tenantId, normalizedBarcode, ProductStatus.ACTIVE.name())
				.map(List::of)
				.orElseGet(List::of);
		}
		else if (normalizedQuery != null) {
			rows = products.searchByText(tenantId, normalizedQuery);
		}
		else {
			rows = products.findByTenantIdAndStatusOrderByNameAsc(tenantId, ProductStatus.ACTIVE.name());
		}
		return rows.stream().map(CatalogueService::toView).toList();
	}

	@Transactional(readOnly = true)
	public Optional<ProductView> findBySku(UUID tenantId, String sku) {
		return products.findByTenantIdAndSku(tenantId, sku).map(CatalogueService::toView);
	}

	@Transactional
	public ProductView create(
		UUID tenantId,
		String sku,
		String name,
		String barcode,
		BigDecimal unitPrice,
		String taxRateCode
	) {
		String normalizedSku = required(sku, "sku");
		String normalizedName = required(name, "name");
		if (unitPrice == null || unitPrice.signum() < 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"unitPrice must be zero or greater");
		}
		if (products.findByTenantIdAndSku(tenantId, normalizedSku).isPresent()) {
			throw duplicateSku();
		}
		Instant now = Instant.now();
		ProductEntity entity = new ProductEntity(
			UUID.randomUUID(),
			tenantId,
			normalizedSku,
			normalizedName,
			blankToNull(barcode),
			unitPrice.setScale(2, RoundingMode.HALF_UP),
			blankToNull(taxRateCode),
			ProductStatus.ACTIVE.name(),
			now,
			now);
		try {
			return toView(products.saveAndFlush(entity));
		}
		catch (DataIntegrityViolationException ex) {
			throw duplicateSku();
		}
	}

	private static ProductView toView(ProductEntity entity) {
		return new ProductView(
			entity.getId(),
			entity.getTenantId(),
			entity.getSku(),
			entity.getName(),
			entity.getBarcode(),
			entity.getUnitPrice(),
			entity.getTaxRateCode(),
			entity.getStatus());
	}

	private static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				field + " must not be blank");
		}
		return value.trim();
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static ApiException notFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found",
			"Product does not exist");
	}

	private static ApiException duplicateSku() {
		return new ApiException(HttpStatus.CONFLICT, "DUPLICATE_SKU", "Duplicate SKU",
			"Product SKU already exists");
	}

}
