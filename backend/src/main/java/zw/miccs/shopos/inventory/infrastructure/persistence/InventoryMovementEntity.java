package zw.miccs.shopos.inventory.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovementEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "quantity_delta", nullable = false, precision = 14, scale = 3)
	private BigDecimal quantityDelta;

	@Column(name = "movement_type", nullable = false)
	private String movementType;

	private String reason;

	private String note;

	@Column(name = "reference_id")
	private UUID referenceId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected InventoryMovementEntity() {
	}

	public InventoryMovementEntity(
		UUID id,
		UUID tenantId,
		UUID branchId,
		UUID productId,
		BigDecimal quantityDelta,
		String movementType,
		String reason,
		String note,
		UUID referenceId,
		Instant createdAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.branchId = branchId;
		this.productId = productId;
		this.quantityDelta = quantityDelta;
		this.movementType = movementType;
		this.reason = reason;
		this.note = note;
		this.referenceId = referenceId;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getBranchId() {
		return branchId;
	}

	public UUID getProductId() {
		return productId;
	}

	public BigDecimal getQuantityDelta() {
		return quantityDelta;
	}

	public String getMovementType() {
		return movementType;
	}

	public String getReason() {
		return reason;
	}

	public String getNote() {
		return note;
	}

	public UUID getReferenceId() {
		return referenceId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
