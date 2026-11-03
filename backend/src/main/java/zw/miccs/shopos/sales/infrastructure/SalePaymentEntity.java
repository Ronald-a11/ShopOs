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
import jakarta.persistence.Table;

import zw.miccs.shopos.sales.domain.PaymentMethod;

@Entity
@Table(name = "sale_payments")
@IdClass(SalePaymentEntity.Pk.class)
public class SalePaymentEntity {

	@Id
	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "sale_id", nullable = false)
	private UUID saleId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod method;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(name = "provider_reference")
	private String providerReference;

	@Column(name = "terminal_id")
	private String terminalId;

	@Column(name = "card_scheme")
	private String cardScheme;

	@Column(name = "masked_pan_last4")
	private String maskedPanLast4;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		createdAt = Instant.now();
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

	public UUID getSaleId() {
		return saleId;
	}

	public void setSaleId(UUID saleId) {
		this.saleId = saleId;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public void setMethod(PaymentMethod method) {
		this.method = method;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getProviderReference() {
		return providerReference;
	}

	public void setProviderReference(String providerReference) {
		this.providerReference = providerReference;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

	public String getCardScheme() {
		return cardScheme;
	}

	public void setCardScheme(String cardScheme) {
		this.cardScheme = cardScheme;
	}

	public String getMaskedPanLast4() {
		return maskedPanLast4;
	}

	public void setMaskedPanLast4(String maskedPanLast4) {
		this.maskedPanLast4 = maskedPanLast4;
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
