package zw.miccs.shopos.catalogue.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import zw.miccs.shopos.catalogue.application.BarcodeInput;
import zw.miccs.shopos.catalogue.domain.BarcodeLookup;
import zw.miccs.shopos.catalogue.domain.Product;
import zw.miccs.shopos.catalogue.domain.ProductBarcode;
import zw.miccs.shopos.catalogue.domain.ProductImportResult;

public final class ProductDtos {

	private ProductDtos() {
	}

	public record ProductBarcodeRequest(
		@NotBlank String barcode,
		@NotBlank String unitMultiplier
	) {
		BarcodeInput toInput() {
			return new BarcodeInput(barcode, new BigDecimal(unitMultiplier));
		}
	}

	public record CreateProductRequest(
		@NotBlank String sku,
		@NotBlank String name,
		String description,
		@NotBlank String taxCode,
		String unitPrice,
		String currency,
		@Valid List<ProductBarcodeRequest> barcodes
	) {
	}

	public record UpdateProductRequest(
		String name,
		String description,
		String taxCode,
		String unitPrice,
		String currency,
		Boolean active,
		@Valid List<ProductBarcodeRequest> barcodes
	) {
	}

	public record ProductBarcodeResponse(String barcode, String unitMultiplier) {
		static ProductBarcodeResponse from(ProductBarcode barcode) {
			return new ProductBarcodeResponse(barcode.barcode(), decimal(barcode.unitMultiplier()));
		}
	}

	public record ProductResponse(
		UUID id,
		String sku,
		String name,
		String description,
		String taxCode,
		String unitPrice,
		String currency,
		boolean active,
		List<ProductBarcodeResponse> barcodes
	) {
		static ProductResponse from(Product product) {
			return new ProductResponse(
				product.id(),
				product.sku(),
				product.name(),
				product.description(),
				product.taxCode(),
				decimal(product.unitPrice()),
				product.currency(),
				product.active(),
				product.barcodes().stream().map(ProductBarcodeResponse::from).toList());
		}
	}

	public record BarcodeLookupResponse(
		ProductResponse product,
		String unitMultiplier,
		String barcode
	) {
		static BarcodeLookupResponse from(BarcodeLookup lookup) {
			return new BarcodeLookupResponse(
				ProductResponse.from(lookup.product()),
				decimal(lookup.unitMultiplier()),
				lookup.barcode());
		}
	}

	public record ImportRowError(int row, String message) {
	}

	public record ProductImportResponse(int created, int updated, List<ImportRowError> errors) {
		static ProductImportResponse from(ProductImportResult result) {
			return new ProductImportResponse(
				result.created(),
				result.updated(),
				result.errors().stream().map(e -> new ImportRowError(e.row(), e.message())).toList());
		}
	}

	static BigDecimal parseOptionalDecimal(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return new BigDecimal(value.trim());
	}

	private static String decimal(BigDecimal value) {
		return value == null ? null : value.stripTrailingZeros().toPlainString();
	}
}
