package zw.miccs.shopos.sales.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales")
public class SaleEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(name = "device_id", nullable = false)
	private UUID deviceId;

	@Column(name = "shift_id", nullable = false)
	private UUID shiftId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "client_sale_id", nullable = false)
	private String clientSaleId;

	@Column(name = "receipt_number", nullable = false)
	private String receiptNumber;

	@Column(nullable = false)
	private String currency;

	@Column(nullable = false)
	private String status;

	@Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected SaleEntity() {
	}

	public SaleEntity(
		UUID id,
		UUID tenantId,
		UUID branchId,
		UUID deviceId,
		UUID shiftId,
		UUID userId,
		String clientSaleId,
		String receiptNumber,
		String currency,
		String status,
		BigDecimal totalAmount,
		Instant createdAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.branchId = branchId;
		this.deviceId = deviceId;
		this.shiftId = shiftId;
		this.userId = userId;
		this.clientSaleId = clientSaleId;
		this.receiptNumber = receiptNumber;
		this.currency = currency;
		this.status = status;
		this.totalAmount = totalAmount;
		this.createdAt = createdAt;
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

	public UUID getShiftId() {
		return shiftId;
	}

	public String getClientSaleId() {
		return clientSaleId;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public String getCurrency() {
		return currency;
	}

	public String getStatus() {
		return status;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
