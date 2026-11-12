package zw.miccs.shopos.tenancy.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "memberships")
public class MembershipEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(nullable = false)
	private String role;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected MembershipEntity() {
	}

	public MembershipEntity(UUID id, UUID tenantId, UUID userId, String role, String status, Instant createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.userId = userId;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getRole() {
		return role;
	}

	public String getStatus() {
		return status;
	}

}
