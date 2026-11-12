package zw.miccs.shopos.inventory.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_balances")
public class InventoryBalanceEntity {

	@EmbeddedId
	private InventoryBalanceId id;

	@Column(nullable = false, precision = 14, scale = 3)
	private BigDecimal quantity;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected InventoryBalanceEntity() {
	}

	public InventoryBalanceEntity(InventoryBalanceId id, BigDecimal quantity, Instant updatedAt) {
		this.id = id;
		this.quantity = quantity;
		this.updatedAt = updatedAt;
	}

	public InventoryBalanceId getId() {
		return id;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

}
