package zw.miccs.shopos.shifts.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cashier_shifts")
public class CashierShiftEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "device_id", nullable = false)
	private UUID deviceId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false)
	private String status;

	@Column(name = "opening_float", nullable = false, precision = 14, scale = 2)
	private BigDecimal openingFloat;

	@Column(name = "counted_cash", precision = 14, scale = 2)
	private BigDecimal countedCash;

	@Column(name = "expected_cash", precision = 14, scale = 2)
	private BigDecimal expectedCash;

	@Column(precision = 14, scale = 2)
	private BigDecimal variance;

	private String note;

	@Column(name = "opened_at", nullable = false)
	private Instant openedAt;

	@Column(name = "closed_at")
	private Instant closedAt;

	protected CashierShiftEntity() {
	}

	public CashierShiftEntity(
		UUID id,
		UUID tenantId,
		UUID branchId,
		UUID deviceId,
		UUID userId,
		String status,
		BigDecimal openingFloat,
		Instant openedAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.branchId = branchId;
		this.deviceId = deviceId;
		this.userId = userId;
		this.status = status;
		this.openingFloat = openingFloat;
		this.openedAt = openedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getBranchId() {
		return branchId;
	}

	public UUID getDeviceId() {
		return deviceId;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getStatus() {
		return status;
	}

	public BigDecimal getOpeningFloat() {
		return openingFloat;
	}

	public BigDecimal getCountedCash() {
		return countedCash;
	}

	public BigDecimal getExpectedCash() {
		return expectedCash;
	}

	public BigDecimal getVariance() {
		return variance;
	}

	public String getNote() {
		return note;
	}

	public Instant getOpenedAt() {
		return openedAt;
	}

	public Instant getClosedAt() {
		return closedAt;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setCountedCash(BigDecimal countedCash) {
		this.countedCash = countedCash;
	}

	public void setExpectedCash(BigDecimal expectedCash) {
		this.expectedCash = expectedCash;
	}

	public void setVariance(BigDecimal variance) {
		this.variance = variance;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setClosedAt(Instant closedAt) {
		this.closedAt = closedAt;
	}

}
