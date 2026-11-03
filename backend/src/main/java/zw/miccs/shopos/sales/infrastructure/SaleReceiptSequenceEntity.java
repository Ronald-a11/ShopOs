package zw.miccs.shopos.sales.infrastructure;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale_receipt_sequences")
@IdClass(SaleReceiptSequenceEntity.Pk.class)
public class SaleReceiptSequenceEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "next_value", nullable = false)
	private long nextValue = 1L;

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

	public long getNextValue() {
		return nextValue;
	}

	public void setNextValue(long nextValue) {
		this.nextValue = nextValue;
	}

	public static class Pk implements Serializable {
		private UUID tenantId;
		private UUID branchId;

		public Pk() {
		}

		public Pk(UUID tenantId, UUID branchId) {
			this.tenantId = tenantId;
			this.branchId = branchId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Pk pk)) {
				return false;
			}
			return Objects.equals(tenantId, pk.tenantId) && Objects.equals(branchId, pk.branchId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tenantId, branchId);
		}
	}
}
