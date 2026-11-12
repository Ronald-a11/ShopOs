package zw.miccs.shopos.catalogue.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

	Optional<ProductEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<ProductEntity> findByTenantIdAndSku(UUID tenantId, String sku);

	Optional<ProductEntity> findFirstByTenantIdAndBarcodeAndStatusOrderByNameAsc(
		UUID tenantId,
		String barcode,
		String status
	);

	List<ProductEntity> findByTenantIdAndStatusOrderByNameAsc(UUID tenantId, String status);

	@Query("""
		SELECT p FROM ProductEntity p
		WHERE p.tenantId = :tenantId
		  AND p.status = 'ACTIVE'
		  AND (
		        LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
		        OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%'))
		        OR LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :q, '%'))
		      )
		ORDER BY p.name
		""")
	List<ProductEntity> searchByText(@Param("tenantId") UUID tenantId, @Param("q") String q);

}
