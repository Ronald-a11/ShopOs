package zw.miccs.shopos.catalogue.application;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import zw.miccs.shopos.catalogue.domain.BarcodeLookup;
import zw.miccs.shopos.catalogue.domain.Product;
import zw.miccs.shopos.catalogue.domain.ProductImportResult;
import zw.miccs.shopos.catalogue.infrastructure.ProductBarcodeEntity;
import zw.miccs.shopos.catalogue.infrastructure.ProductBarcodeJpaRepository;
import zw.miccs.shopos.catalogue.infrastructure.ProductEntity;
import zw.miccs.shopos.catalogue.infrastructure.ProductJpaRepository;
import zw.miccs.shopos.common.api.ConflictException;
import zw.miccs.shopos.common.api.ForbiddenException;
import zw.miccs.shopos.common.api.NotFoundException;
import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.common.api.UnprocessableEntityException;
import zw.miccs.shopos.tenancy.application.TenantAccess;
import zw.miccs.shopos.tenancy.application.TenantContext;

@Service
@Transactional
public class CatalogueServiceImpl implements CatalogueService {

	private static final int MAX_IMPORT_ROWS = 5_000;
	private static final String EXPECTED_HEADER =
		"sku,name,description,taxCode,unitPrice,currency,barcode,unitMultiplier";

	private final ProductJpaRepository products;
	private final ProductBarcodeJpaRepository barcodes;
	private final TenantAccess tenantAccess;

	public CatalogueServiceImpl(
		ProductJpaRepository products,
		ProductBarcodeJpaRepository barcodes,
		TenantAccess tenantAccess) {
		this.products = products;
		this.barcodes = barcodes;
		this.tenantAccess = tenantAccess;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<Product> search(String q, Pageable pageable) {
		UUID tenantId = tenantAccess.requireTenantId();
		Page<ProductEntity> page;
		if (q == null || q.isBlank()) {
			page = products.findByTenantId(tenantId, pageable);
		}
		else {
			page = products.search(tenantId, q.trim(), pageable);
		}
		return PageResponse.from(page.map(this::toProduct));
	}

	@Override
	public Product create(
		String sku,
		String name,
		String description,
		String taxCode,
		BigDecimal unitPrice,
		String currency,
		List<BarcodeInput> barcodeInputs) {
		requireManage();
		UUID tenantId = tenantAccess.requireTenantId();
		String normalizedSku = sku.trim();
		if (products.existsByTenantIdAndSkuIgnoreCase(tenantId, normalizedSku)) {
			throw new ConflictException("PRODUCT_SKU_EXISTS", "Product SKU already exists");
		}
		ProductEntity entity = new ProductEntity();
		entity.setTenantId(tenantId);
		entity.setSku(normalizedSku);
		entity.setName(name.trim());
		entity.setDescription(blankToNull(description));
		entity.setTaxCode(taxCode.trim());
		entity.setUnitPrice(unitPrice);
		entity.setCurrency(blankToNull(currency) == null ? null : currency.trim().toUpperCase(Locale.ROOT));
		entity.setActive(true);
		try {
			ProductEntity saved = products.save(entity);
			replaceBarcodes(tenantId, saved.getId(), barcodeInputs == null ? List.of() : barcodeInputs);
			return toProduct(saved);
		}
		catch (DataIntegrityViolationException ex) {
			throw new ConflictException("PRODUCT_SKU_EXISTS", "Product SKU already exists");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Product get(UUID productId) {
		return toProduct(requireProduct(productId));
	}

	@Override
	public Product update(
		UUID productId,
		String name,
		String description,
		String taxCode,
		BigDecimal unitPrice,
		String currency,
		Boolean active,
		List<BarcodeInput> barcodeInputs) {
		requireManage();
		ProductEntity entity = requireProduct(productId);
		if (name != null) {
			entity.setName(name.trim());
		}
		if (description != null) {
			entity.setDescription(blankToNull(description));
		}
		if (taxCode != null) {
			entity.setTaxCode(taxCode.trim());
		}
		if (unitPrice != null) {
			entity.setUnitPrice(unitPrice);
		}
		if (currency != null) {
			entity.setCurrency(blankToNull(currency) == null ? null : currency.trim().toUpperCase(Locale.ROOT));
		}
		if (active != null) {
			entity.setActive(active);
		}
		ProductEntity saved = products.save(entity);
		if (barcodeInputs != null) {
			replaceBarcodes(saved.getTenantId(), saved.getId(), barcodeInputs);
		}
		return toProduct(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public BarcodeLookup lookupByBarcode(String barcode) {
		UUID tenantId = tenantAccess.requireTenantId();
		ProductBarcodeEntity barcodeEntity = barcodes.findByTenantIdAndBarcode(tenantId, barcode.trim())
			.orElseThrow(() -> new NotFoundException("BARCODE_NOT_FOUND", "Barcode not found"));
		ProductEntity product = products.findByTenantIdAndId(tenantId, barcodeEntity.getProductId())
			.filter(ProductEntity::isActive)
			.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
		return new BarcodeLookup(toProduct(product), barcodeEntity.getUnitMultiplier(), barcodeEntity.getBarcode());
	}

	@Override
	public ProductImportResult importCsv(MultipartFile file) {
		requireManage();
		if (file == null || file.isEmpty()) {
			throw new UnprocessableEntityException("IMPORT_INVALID", "CSV file is required");
		}
		UUID tenantId = tenantAccess.requireTenantId();
		int created = 0;
		int updated = 0;
		List<ProductImportResult.RowError> errors = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			if (header == null || !normalizeHeader(header).equalsIgnoreCase(EXPECTED_HEADER)) {
				throw new UnprocessableEntityException(
					"IMPORT_INVALID",
					"CSV header must be: " + EXPECTED_HEADER);
			}
			String line;
			int row = 1;
			int dataRows = 0;
			while ((line = reader.readLine()) != null) {
				row++;
				if (line.isBlank()) {
					continue;
				}
				dataRows++;
				if (dataRows > MAX_IMPORT_ROWS) {
					errors.add(new ProductImportResult.RowError(row, "Import exceeds " + MAX_IMPORT_ROWS + " rows"));
					break;
				}
				try {
					boolean wasCreate = upsertImportRow(tenantId, line);
					if (wasCreate) {
						created++;
					}
					else {
						updated++;
					}
				}
				catch (RuntimeException ex) {
					errors.add(new ProductImportResult.RowError(row, ex.getMessage() == null ? "Invalid row" : ex.getMessage()));
				}
			}
		}
		catch (UnprocessableEntityException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new UnprocessableEntityException("IMPORT_INVALID", "Unable to read CSV file");
		}
		return new ProductImportResult(created, updated, errors);
	}

	private boolean upsertImportRow(UUID tenantId, String line) {
		String[] cols = parseCsvLine(line);
		if (cols.length < 8) {
			throw new IllegalArgumentException("Expected 8 columns");
		}
		String sku = require(cols[0], "sku");
		String name = require(cols[1], "name");
		String description = blankToNull(cols[2]);
		String taxCode = require(cols[3], "taxCode");
		BigDecimal unitPrice = parseDecimal(cols[4], "unitPrice", false);
		String currency = blankToNull(cols[5]);
		String barcode = blankToNull(cols[6]);
		BigDecimal multiplier = parseDecimal(cols[7], "unitMultiplier", true);

		Optional<ProductEntity> existing = products.findByTenantIdAndSkuIgnoreCase(tenantId, sku);
		ProductEntity entity;
		boolean create;
		if (existing.isPresent()) {
			entity = existing.get();
			entity.setName(name);
			entity.setDescription(description);
			entity.setTaxCode(taxCode);
			entity.setUnitPrice(unitPrice);
			entity.setCurrency(currency == null ? null : currency.toUpperCase(Locale.ROOT));
			create = false;
		}
		else {
			entity = new ProductEntity();
			entity.setTenantId(tenantId);
			entity.setSku(sku);
			entity.setName(name);
			entity.setDescription(description);
			entity.setTaxCode(taxCode);
			entity.setUnitPrice(unitPrice);
			entity.setCurrency(currency == null ? null : currency.toUpperCase(Locale.ROOT));
			entity.setActive(true);
			create = true;
		}
		ProductEntity saved = products.save(entity);
		if (barcode != null) {
			if (multiplier == null || multiplier.compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("unitMultiplier must be > 0 when barcode is set");
			}
			Optional<ProductBarcodeEntity> existingBarcode = barcodes.findByTenantIdAndBarcode(tenantId, barcode);
			if (existingBarcode.isPresent() && !existingBarcode.get().getProductId().equals(saved.getId())) {
				throw new IllegalArgumentException("Barcode already assigned to another product");
			}
			ProductBarcodeEntity barcodeEntity = existingBarcode.orElseGet(ProductBarcodeEntity::new);
			barcodeEntity.setTenantId(tenantId);
			barcodeEntity.setBarcode(barcode);
			barcodeEntity.setProductId(saved.getId());
			barcodeEntity.setUnitMultiplier(multiplier);
			barcodes.save(barcodeEntity);
		}
		return create;
	}

	private void replaceBarcodes(UUID tenantId, UUID productId, List<BarcodeInput> inputs) {
		Set<String> seen = new HashSet<>();
		for (BarcodeInput input : inputs) {
			if (input == null || input.barcode() == null || input.barcode().isBlank()) {
				throw new UnprocessableEntityException("VALIDATION_FAILED", "Barcode is required");
			}
			if (input.unitMultiplier() == null || input.unitMultiplier().compareTo(BigDecimal.ZERO) <= 0) {
				throw new UnprocessableEntityException("VALIDATION_FAILED", "unitMultiplier must be > 0");
			}
			String code = input.barcode().trim();
			if (!seen.add(code.toLowerCase(Locale.ROOT))) {
				throw new ConflictException("BARCODE_EXISTS", "Duplicate barcode in request: " + code);
			}
			Optional<ProductBarcodeEntity> existing = barcodes.findByTenantIdAndBarcode(tenantId, code);
			if (existing.isPresent() && !existing.get().getProductId().equals(productId)) {
				throw new ConflictException("BARCODE_EXISTS", "Barcode already exists: " + code);
			}
		}
		barcodes.deleteByTenantIdAndProductId(tenantId, productId);
		for (BarcodeInput input : inputs) {
			ProductBarcodeEntity entity = new ProductBarcodeEntity();
			entity.setTenantId(tenantId);
			entity.setBarcode(input.barcode().trim());
			entity.setProductId(productId);
			entity.setUnitMultiplier(input.unitMultiplier());
			barcodes.save(entity);
		}
	}

	private ProductEntity requireProduct(UUID productId) {
		return products.findByTenantIdAndId(tenantAccess.requireTenantId(), productId)
			.orElseThrow(() -> new NotFoundException("PRODUCT_NOT_FOUND", "Product not found"));
	}

	private Product toProduct(ProductEntity entity) {
		List<ProductBarcodeEntity> list =
			barcodes.findByTenantIdAndProductIdOrderByBarcodeAsc(entity.getTenantId(), entity.getId());
		return CatalogueMapper.toDomain(entity, list);
	}

	private void requireManage() {
		if (!TenantContext.require().role().canManageCatalogue()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to manage catalogue");
		}
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static String require(String value, String field) {
		String trimmed = blankToNull(value);
		if (trimmed == null) {
			throw new IllegalArgumentException(field + " is required");
		}
		return trimmed;
	}

	private static BigDecimal parseDecimal(String value, String field, boolean allowBlank) {
		String trimmed = blankToNull(value);
		if (trimmed == null) {
			if (allowBlank) {
				return null;
			}
			throw new IllegalArgumentException(field + " is required");
		}
		try {
			return new BigDecimal(trimmed);
		}
		catch (NumberFormatException ex) {
			throw new IllegalArgumentException(field + " is not a valid number");
		}
	}

	private static String normalizeHeader(String header) {
		return header.replace("\uFEFF", "").trim().replace(" ", "");
	}

	private static String[] parseCsvLine(String line) {
		List<String> values = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				inQuotes = !inQuotes;
			}
			else if (c == ',' && !inQuotes) {
				values.add(current.toString().trim());
				current.setLength(0);
			}
			else {
				current.append(c);
			}
		}
		values.add(current.toString().trim());
		return values.toArray(String[]::new);
	}
}
