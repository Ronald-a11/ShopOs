package zw.miccs.shopos.identity.infrastructure;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/** Read-only projection of pos_devices for login device binding. */
@Entity
@Table(name = "pos_devices")
@IdClass(PosDeviceLookupEntity.Pk.class)
public class PosDeviceLookupEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(nullable = false)
	private String status;

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getId() {
		return id;
	}

	public UUID getBranchId() {
		return branchId;
	}

	public String getStatus() {
		return status;
	}

	public static class Pk implements Serializable {
		private UUID tenantId;
		private UUID id;

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Pk pk)) {
				return false;
			}
			return Objects.equals(tenantId, pk.tenantId) && Objects.equals(id, pk.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tenantId, id);
		}
	}
}
