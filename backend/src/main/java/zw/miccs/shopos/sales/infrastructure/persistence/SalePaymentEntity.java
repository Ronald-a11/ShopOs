package zw.miccs.shopos.sales.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale_payments")
public class SalePaymentEntity {

	@Id
	private UUID id;

	@Column(name = "sale_id", nullable = false)
	private UUID saleId;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(nullable = false)
	private String method;

	@Column(nullable = false, precision = 14, scale = 2)
	private BigDecimal amount;

	@Column(name = "provider_reference")
	private String providerReference;

	protected SalePaymentEntity() {
	}

	public SalePaymentEntity(
		UUID id,
		UUID saleId,
		UUID tenantId,
		String method,
		BigDecimal amount,
		String providerReference
	) {
		this.id = id;
		this.saleId = saleId;
		this.tenantId = tenantId;
		this.method = method;
		this.amount = amount;
		this.providerReference = providerReference;
	}

	public String getMethod() {
		return method;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getProviderReference() {
		return providerReference;
	}

}
