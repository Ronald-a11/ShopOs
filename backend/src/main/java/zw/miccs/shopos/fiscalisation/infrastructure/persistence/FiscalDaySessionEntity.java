package zw.miccs.shopos.fiscalisation.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fiscal_day_sessions")
public class FiscalDaySessionEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(nullable = false)
	private String status;

	@Column(name = "opened_at", nullable = false)
	private Instant openedAt;

	@Column(name = "closed_at")
	private Instant closedAt;

	protected FiscalDaySessionEntity() {
	}

	public FiscalDaySessionEntity(UUID id, UUID tenantId, String status, Instant openedAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.status = status;
		this.openedAt = openedAt;
	}

	public UUID getId() {
		return id;
	}

	public String getStatus() {
		return status;
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

	public void setClosedAt(Instant closedAt) {
		this.closedAt = closedAt;
	}

}
