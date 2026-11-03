package zw.miccs.shopos.catalogue.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import zw.miccs.shopos.catalogue.domain.BarcodeLookup;
import zw.miccs.shopos.catalogue.domain.Product;
import zw.miccs.shopos.catalogue.domain.ProductImportResult;
import zw.miccs.shopos.common.api.PageResponse;

public interface CatalogueService {

	PageResponse<Product> search(String q, Pageable pageable);

	Product create(
		String sku,
		String name,
		String description,
		String taxCode,
		BigDecimal unitPrice,
		String currency,
		List<BarcodeInput> barcodes);

	Product get(UUID productId);

	Product update(
		UUID productId,
		String name,
		String description,
		String taxCode,
		BigDecimal unitPrice,
		String currency,
		Boolean active,
		List<BarcodeInput> barcodes);

	BarcodeLookup lookupByBarcode(String barcode);

	ProductImportResult importCsv(MultipartFile file);
}
