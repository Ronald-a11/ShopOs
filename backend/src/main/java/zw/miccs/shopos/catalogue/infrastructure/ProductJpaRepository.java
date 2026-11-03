package zw.miccs.shopos.catalogue.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, ProductEntity.Pk> {

	Optional<ProductEntity> findByTenantIdAndId(UUID tenantId, UUID id);

	Optional<ProductEntity> findByTenantIdAndSkuIgnoreCase(UUID tenantId, String sku);

	boolean existsByTenantIdAndSkuIgnoreCase(UUID tenantId, String sku);

	Page<ProductEntity> findByTenantId(UUID tenantId, Pageable pageable);

	@Query("""
		select distinct p from ProductEntity p
		left join ProductBarcodeEntity b on b.tenantId = p.tenantId and b.productId = p.id
		where p.tenantId = :tenantId
		  and (
		    lower(p.sku) like lower(concat('%', :q, '%'))
		    or lower(p.name) like lower(concat('%', :q, '%'))
		    or lower(b.barcode) like lower(concat('%', :q, '%'))
		  )
		""")
	Page<ProductEntity> search(@Param("tenantId") UUID tenantId, @Param("q") String q, Pageable pageable);
}
