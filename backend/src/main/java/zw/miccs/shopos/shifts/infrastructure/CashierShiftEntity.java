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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import zw.miccs.shopos.shifts.domain.ShiftStatus;

@Entity
@Table(name = "cashier_shifts")
@IdClass(CashierShiftEntity.Pk.class)
public class CashierShiftEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "device_id", nullable = false)
	private UUID deviceId;

	@Column(name = "cashier_user_id", nullable = false)
	private UUID cashierUserId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ShiftStatus status;

	@Column(name = "opening_float", nullable = false, precision = 19, scale = 4)
	private BigDecimal openingFloat;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "expected_cash", nullable = false, precision = 19, scale = 4)
	private BigDecimal expectedCash;

	@Column(name = "counted_cash", precision = 19, scale = 4)
	private BigDecimal countedCash;

	@Column(precision = 19, scale = 4)
	private BigDecimal variance;

	@Column(name = "approval_reason")
	private String approvalReason;

	@Column(name = "opened_at", nullable = false)
	private Instant openedAt;

	@Column(name = "closed_at")
	private Instant closedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (openedAt == null) {
			openedAt = now;
		}
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
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

	public UUID getBranchId() {
		return branchId;
	}

	public void setBranchId(UUID branchId) {
		this.branchId = branchId;
	}

	public UUID getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(UUID deviceId) {
		this.deviceId = deviceId;
	}

	public UUID getCashierUserId() {
		return cashierUserId;
	}

	public void setCashierUserId(UUID cashierUserId) {
		this.cashierUserId = cashierUserId;
	}

	public ShiftStatus getStatus() {
		return status;
	}

	public void setStatus(ShiftStatus status) {
		this.status = status;
	}

	public BigDecimal getOpeningFloat() {
		return openingFloat;
	}

	public void setOpeningFloat(BigDecimal openingFloat) {
		this.openingFloat = openingFloat;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getExpectedCash() {
		return expectedCash;
	}

	public void setExpectedCash(BigDecimal expectedCash) {
		this.expectedCash = expectedCash;
	}

	public BigDecimal getCountedCash() {
		return countedCash;
	}

	public void setCountedCash(BigDecimal countedCash) {
		this.countedCash = countedCash;
	}

	public BigDecimal getVariance() {
		return variance;
	}

	public void setVariance(BigDecimal variance) {
		this.variance = variance;
	}

	public String getApprovalReason() {
		return approvalReason;
	}

	public void setApprovalReason(String approvalReason) {
		this.approvalReason = approvalReason;
	}

	public Instant getOpenedAt() {
		return openedAt;
	}

	public void setOpenedAt(Instant openedAt) {
		this.openedAt = openedAt;
	}

	public Instant getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Instant closedAt) {
		this.closedAt = closedAt;
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
