package zw.miccs.shopos.fiscalisation.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fiscal_documents")
public class FiscalDocumentEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "sale_id", nullable = false)
	private UUID saleId;

	@Column(nullable = false)
	private String status;

	@Column(nullable = false)
	private int attempts;

	@Column(name = "last_error")
	private String lastError;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected FiscalDocumentEntity() {
	}

	public FiscalDocumentEntity(
		UUID id,
		UUID tenantId,
		UUID saleId,
		String status,
		int attempts,
		String lastError,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.saleId = saleId;
		this.status = status;
		this.attempts = attempts;
		this.lastError = lastError;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getSaleId() {
		return saleId;
	}

	public String getStatus() {
		return status;
	}

	public int getAttempts() {
		return attempts;
	}

	public String getLastError() {
		return lastError;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

}
