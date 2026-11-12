package zw.miccs.shopos.catalogue.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(nullable = false)
	private String sku;

	@Column(nullable = false)
	private String name;

	private String barcode;

	@Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "tax_rate_code")
	private String taxRateCode;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected ProductEntity() {
	}

	public ProductEntity(
		UUID id,
		UUID tenantId,
		String sku,
		String name,
		String barcode,
		BigDecimal unitPrice,
		String taxRateCode,
		String status,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.sku = sku;
		this.name = name;
		this.barcode = barcode;
		this.unitPrice = unitPrice;
		this.taxRateCode = taxRateCode;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public String getSku() {
		return sku;
	}

	public String getName() {
		return name;
	}

	public String getBarcode() {
		return barcode;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public String getTaxRateCode() {
		return taxRateCode;
	}

	public String getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
