package zw.miccs.shopos.shifts.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cash_movements")
public class CashMovementEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "shift_id", nullable = false)
	private UUID shiftId;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal amount;

	private String note;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected CashMovementEntity() {
	}

	public CashMovementEntity(
		UUID id,
		UUID tenantId,
		UUID shiftId,
		String type,
		BigDecimal amount,
		String note,
		Instant createdAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.shiftId = shiftId;
		this.type = type;
		this.amount = amount;
		this.note = note;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getShiftId() {
		return shiftId;
	}

	public String getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getNote() {
		return note;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
