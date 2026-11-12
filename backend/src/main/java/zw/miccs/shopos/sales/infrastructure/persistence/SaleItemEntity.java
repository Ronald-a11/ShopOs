package zw.miccs.shopos.sales.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale_items")
public class SaleItemEntity {

	@Id
	private UUID id;

	@Column(name = "sale_id", nullable = false)
	private UUID saleId;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(nullable = false, precision = 14, scale = 3)
	private BigDecimal quantity;

	@Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "line_total", nullable = false, precision = 14, scale = 2)
	private BigDecimal lineTotal;

	protected SaleItemEntity() {
	}

	public SaleItemEntity(
		UUID id,
		UUID saleId,
		UUID tenantId,
		UUID productId,
		BigDecimal quantity,
		BigDecimal unitPrice,
		BigDecimal lineTotal
	) {
		this.id = id;
		this.saleId = saleId;
		this.tenantId = tenantId;
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.lineTotal = lineTotal;
	}

	public UUID getProductId() {
		return productId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getLineTotal() {
		return lineTotal;
	}

}
