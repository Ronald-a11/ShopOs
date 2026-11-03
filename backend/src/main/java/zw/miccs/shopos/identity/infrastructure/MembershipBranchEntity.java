package zw.miccs.shopos.identity.infrastructure;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_branches")
@IdClass(MembershipBranchEntity.Pk.class)
public class MembershipBranchEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Id
	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getBranchId() {
		return branchId;
	}

	public void setBranchId(UUID branchId) {
		this.branchId = branchId;
	}

	public static class Pk implements Serializable {
		private UUID tenantId;
		private UUID userId;
		private UUID branchId;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Pk pk)) {
				return false;
			}
			return Objects.equals(tenantId, pk.tenantId)
				&& Objects.equals(userId, pk.userId)
				&& Objects.equals(branchId, pk.branchId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tenantId, userId, branchId);
		}
	}
}
