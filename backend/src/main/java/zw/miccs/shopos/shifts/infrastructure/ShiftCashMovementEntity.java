package zw.miccs.shopos.shifts.infrastructure;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import zw.miccs.shopos.shifts.domain.CashMovementType;

@Entity
@Table(name = "shift_cash_movements")
@IdClass(ShiftCashMovementEntity.Pk.class)
public class ShiftCashMovementEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "shift_id", nullable = false)
	private UUID shiftId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CashMovementType type;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currency;

	private String reason;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (occurredAt == null) {
			occurredAt = now;
		}
		createdAt = now;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getShiftId() {
		return shiftId;
	}

	public void setShiftId(UUID shiftId) {
		this.shiftId = shiftId;
	}

	public CashMovementType getType() {
		return type;
	}

	public void setType(CashMovementType type) {
		this.type = type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(Instant occurredAt) {
		this.occurredAt = occurredAt;
	}

	public static class Pk implements Serializable {
		private UUID tenantId;
		private UUID id;

		public Pk() {
		}

		public Pk(UUID tenantId, UUID id) {
			this.tenantId = tenantId;
			this.id = id;
		}

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
