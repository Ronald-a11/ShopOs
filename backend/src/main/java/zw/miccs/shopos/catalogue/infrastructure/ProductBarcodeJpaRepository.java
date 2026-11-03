package zw.miccs.shopos.catalogue.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductBarcodeJpaRepository extends JpaRepository<ProductBarcodeEntity, ProductBarcodeEntity.Pk> {

	List<ProductBarcodeEntity> findByTenantIdAndProductIdOrderByBarcodeAsc(UUID tenantId, UUID productId);

	Optional<ProductBarcodeEntity> findByTenantIdAndBarcode(UUID tenantId, String barcode);

	boolean existsByTenantIdAndBarcode(UUID tenantId, String barcode);

	void deleteByTenantIdAndProductId(UUID tenantId, UUID productId);
}
