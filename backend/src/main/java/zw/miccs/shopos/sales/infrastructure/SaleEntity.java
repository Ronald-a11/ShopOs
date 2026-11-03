package zw.miccs.shopos.sales.infrastructure;

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

import zw.miccs.shopos.sales.domain.FiscalStatus;
import zw.miccs.shopos.sales.domain.SaleStatus;

@Entity
@Table(name = "sales")
@IdClass(SaleEntity.Pk.class)
public class SaleEntity {

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

	@Column(name = "client_sale_id")
	private UUID clientSaleId;

	@Column(name = "receipt_number", nullable = false)
	private String receiptNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SaleStatus status;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "cashier_user_id", nullable = false)
	private UUID cashierUserId;

	@Column(name = "shift_id", nullable = false)
	private UUID shiftId;

	@Column(name = "original_sale_id")
	private UUID originalSaleId;

	@Column(name = "config_version", nullable = false)
	private int configVersion;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal subtotal;

	@Column(name = "discount_total", nullable = false, precision = 19, scale = 4)
	private BigDecimal discountTotal = BigDecimal.ZERO;

	@Column(name = "tax_total", nullable = false, precision = 19, scale = 4)
	private BigDecimal taxTotal = BigDecimal.ZERO;

	@Column(name = "grand_total", nullable = false, precision = 19, scale = 4)
	private BigDecimal grandTotal;

	@Column(name = "change_amount", precision = 19, scale = 4)
	private BigDecimal changeAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "fiscal_status", nullable = false)
	private FiscalStatus fiscalStatus = FiscalStatus.PENDING_SUBMISSION;

	private String reason;

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

	public UUID getClientSaleId() {
		return clientSaleId;
	}

	public void setClientSaleId(UUID clientSaleId) {
		this.clientSaleId = clientSaleId;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

	public SaleStatus getStatus() {
		return status;
	}

	public void setStatus(SaleStatus status) {
		this.status = status;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public void setOccurredAt(Instant occurredAt) {
		this.occurredAt = occurredAt;
	}

	public UUID getCashierUserId() {
		return cashierUserId;
	}

	public void setCashierUserId(UUID cashierUserId) {
		this.cashierUserId = cashierUserId;
	}

	public UUID getShiftId() {
		return shiftId;
	}

	public void setShiftId(UUID shiftId) {
		this.shiftId = shiftId;
	}

	public UUID getOriginalSaleId() {
		return originalSaleId;
	}

	public void setOriginalSaleId(UUID originalSaleId) {
		this.originalSaleId = originalSaleId;
	}

	public int getConfigVersion() {
		return configVersion;
	}

	public void setConfigVersion(int configVersion) {
		this.configVersion = configVersion;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getDiscountTotal() {
		return discountTotal;
	}

	public void setDiscountTotal(BigDecimal discountTotal) {
		this.discountTotal = discountTotal;
	}

	public BigDecimal getTaxTotal() {
		return taxTotal;
	}

	public void setTaxTotal(BigDecimal taxTotal) {
		this.taxTotal = taxTotal;
	}

	public BigDecimal getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(BigDecimal grandTotal) {
		this.grandTotal = grandTotal;
	}

	public BigDecimal getChangeAmount() {
		return changeAmount;
	}

	public void setChangeAmount(BigDecimal changeAmount) {
		this.changeAmount = changeAmount;
	}

	public FiscalStatus getFiscalStatus() {
		return fiscalStatus;
	}

	public void setFiscalStatus(FiscalStatus fiscalStatus) {
		this.fiscalStatus = fiscalStatus;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
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
