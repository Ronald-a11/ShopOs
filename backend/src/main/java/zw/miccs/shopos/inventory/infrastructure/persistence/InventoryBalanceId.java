package zw.miccs.shopos.inventory.infrastructure.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class InventoryBalanceId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	protected InventoryBalanceId() {
	}

	public InventoryBalanceId(UUID tenantId, UUID branchId, UUID productId) {
		this.tenantId = tenantId;
		this.branchId = branchId;
		this.productId = productId;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getBranchId() {
		return branchId;
	}

	public UUID getProductId() {
		return productId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof InventoryBalanceId that)) {
			return false;
		}
		return Objects.equals(tenantId, that.tenantId)
			&& Objects.equals(branchId, that.branchId)
			&& Objects.equals(productId, that.productId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tenantId, branchId, productId);
	}

}
