package zw.miccs.shopos.catalogue.application;

import java.util.List;

import zw.miccs.shopos.catalogue.domain.Product;
import zw.miccs.shopos.catalogue.domain.ProductBarcode;
import zw.miccs.shopos.catalogue.infrastructure.ProductBarcodeEntity;
import zw.miccs.shopos.catalogue.infrastructure.ProductEntity;

final class CatalogueMapper {

	private CatalogueMapper() {
	}

	static Product toDomain(ProductEntity entity, List<ProductBarcodeEntity> barcodes) {
		return new Product(
			entity.getId(),
			entity.getTenantId(),
			entity.getSku(),
			entity.getName(),
			entity.getDescription(),
			entity.getTaxCode(),
			entity.getUnitPrice(),
			entity.getCurrency(),
			entity.isActive(),
			barcodes.stream()
				.map(b -> new ProductBarcode(b.getBarcode(), b.getUnitMultiplier()))
				.toList());
	}
}
