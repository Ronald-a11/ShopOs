package zw.miccs.shopos.catalogue.infrastructure;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_barcodes")
@IdClass(ProductBarcodeEntity.Pk.class)
public class ProductBarcodeEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(nullable = false)
	private String barcode;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "unit_multiplier", nullable = false, precision = 19, scale = 4)
	private BigDecimal unitMultiplier;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public BigDecimal getUnitMultiplier() {
		return unitMultiplier;
	}

	public void setUnitMultiplier(BigDecimal unitMultiplier) {
		this.unitMultiplier = unitMultiplier;
	}

	public static class Pk implements Serializable {
		private UUID tenantId;
		private String barcode;

		public Pk() {
		}

		public Pk(UUID tenantId, String barcode) {
			this.tenantId = tenantId;
			this.barcode = barcode;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Pk pk)) {
				return false;
			}
			return Objects.equals(tenantId, pk.tenantId) && Objects.equals(barcode, pk.barcode);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tenantId, barcode);
		}
	}
}
