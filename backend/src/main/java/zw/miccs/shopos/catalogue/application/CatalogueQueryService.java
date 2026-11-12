package zw.miccs.shopos.catalogue.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public catalogue lookups used by sales, inventory, and synchronisation.
 */
public interface CatalogueQueryService {

	ProductView findActiveProduct(UUID tenantId, UUID productId);

	Optional<ProductView> findProduct(UUID tenantId, UUID productId);

	Optional<ProductView> findByBarcode(UUID tenantId, String barcode);

	List<ProductView> searchProducts(UUID tenantId, String q, String barcode);

}
