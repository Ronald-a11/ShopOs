package zw.miccs.shopos.inventory.infrastructure;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_balances")
@IdClass(InventoryBalanceEntity.Pk.class)
public class InventoryBalanceEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Id
	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal quantity = BigDecimal.ZERO;

	@Column(name = "reorder_level", precision = 19, scale = 4)
	private BigDecimal reorderLevel;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	@PreUpdate
	void touch() {
		updatedAt = Instant.now();
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public UUID getBranchId() {
		return branchId;
	}

	public void setBranchId(UUID branchId) {
		this.branchId = branchId;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getReorderLevel() {
		return reorderLevel;
	}

	public void setReorderLevel(BigDecimal reorderLevel) {
		this.reorderLevel = reorderLevel;
	}

	public static class Pk implements Serializable {
		private UUID tenantId;
		private UUID branchId;
		private UUID productId;

		public Pk() {
		}

		public Pk(UUID tenantId, UUID branchId, UUID productId) {
			this.tenantId = tenantId;
			this.branchId = branchId;
			this.productId = productId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Pk pk)) {
				return false;
			}
			return Objects.equals(tenantId, pk.tenantId)
				&& Objects.equals(branchId, pk.branchId)
				&& Objects.equals(productId, pk.productId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tenantId, branchId, productId);
		}
	}
}
