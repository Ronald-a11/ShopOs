package zw.miccs.shopos.sales.infrastructure;

import java.io.Serializable;
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
@Table(name = "sale_operations")
@IdClass(SaleOperationEntity.Pk.class)
public class SaleOperationEntity {

	public static final String TYPE_RETURN = "RETURN";
	public static final String TYPE_VOID = "VOID";

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(name = "client_operation_id", nullable = false)
	private UUID clientOperationId;

	@Column(name = "operation_type", nullable = false)
	private String operationType;

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;

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

	public UUID getClientOperationId() {
		return clientOperationId;
	}

	public void setClientOperationId(UUID clientOperationId) {
		this.clientOperationId = clientOperationId;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public UUID getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(UUID aggregateId) {
		this.aggregateId = aggregateId;
	}

	public static class Pk implements Serializable {
		private UUID tenantId;
		private UUID clientOperationId;

		public Pk() {
		}

		public Pk(UUID tenantId, UUID clientOperationId) {
			this.tenantId = tenantId;
			this.clientOperationId = clientOperationId;
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
				&& Objects.equals(clientOperationId, pk.clientOperationId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tenantId, clientOperationId);
		}
	}
}
