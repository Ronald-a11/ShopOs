package zw.miccs.shopos.catalogue.api;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import zw.miccs.shopos.catalogue.api.ProductDtos.BarcodeLookupResponse;
import zw.miccs.shopos.catalogue.api.ProductDtos.CreateProductRequest;
import zw.miccs.shopos.catalogue.api.ProductDtos.ProductImportResponse;
import zw.miccs.shopos.catalogue.api.ProductDtos.ProductResponse;
import zw.miccs.shopos.catalogue.api.ProductDtos.UpdateProductRequest;
import zw.miccs.shopos.catalogue.application.BarcodeInput;
import zw.miccs.shopos.catalogue.application.CatalogueService;
import zw.miccs.shopos.catalogue.domain.Product;
import zw.miccs.shopos.common.api.PageResponse;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final CatalogueService catalogueService;

	public ProductController(CatalogueService catalogueService) {
		this.catalogueService = catalogueService;
	}

	@GetMapping
	PageResponse<ProductResponse> search(
		@RequestParam(required = false) String q,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort) {
		PageResponse<Product> result = catalogueService.search(q, toPageable(page, size, sort));
		return new PageResponse<>(
			result.content().stream().map(ProductResponse::from).toList(),
			result.page());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
		List<BarcodeInput> barcodes = request.barcodes() == null
			? List.of()
			: request.barcodes().stream().map(ProductDtos.ProductBarcodeRequest::toInput).toList();
		return ProductResponse.from(catalogueService.create(
			request.sku(),
			request.name(),
			request.description(),
			request.taxCode(),
			ProductDtos.parseOptionalDecimal(request.unitPrice()),
			request.currency(),
			barcodes));
	}

	@GetMapping("/{id}")
	ProductResponse get(@PathVariable UUID id) {
		return ProductResponse.from(catalogueService.get(id));
	}

	@PatchMapping("/{id}")
	ProductResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
		List<BarcodeInput> barcodes = request.barcodes() == null
			? null
			: request.barcodes().stream().map(ProductDtos.ProductBarcodeRequest::toInput).toList();
		return ProductResponse.from(catalogueService.update(
			id,
			request.name(),
			request.description(),
			request.taxCode(),
			ProductDtos.parseOptionalDecimal(request.unitPrice()),
			request.currency(),
			request.active(),
			barcodes));
	}

	@PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ProductImportResponse importCsv(@RequestPart("file") MultipartFile file) {
		return ProductImportResponse.from(catalogueService.importCsv(file));
	}

	@GetMapping("/barcode/{barcode}")
	BarcodeLookupResponse lookupBarcode(@PathVariable String barcode) {
		return BarcodeLookupResponse.from(catalogueService.lookupByBarcode(barcode));
	}

	private static Pageable toPageable(int page, int size, String sort) {
		int safeSize = Math.min(Math.max(size, 1), 200);
		int safePage = Math.max(page, 0);
		if (sort == null || sort.isBlank()) {
			return PageRequest.of(safePage, safeSize, Sort.by("sku").ascending());
		}
		String[] parts = sort.split(",");
		String property = parts[0].trim();
		Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
			? Sort.Direction.DESC
			: Sort.Direction.ASC;
		return PageRequest.of(safePage, safeSize, Sort.by(direction, property));
	}
}
