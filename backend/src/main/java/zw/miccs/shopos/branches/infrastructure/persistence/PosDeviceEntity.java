package zw.miccs.shopos.branches.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pos_devices")
public class PosDeviceEntity {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "branch_id", nullable = false)
	private UUID branchId;

	@Column(nullable = false)
	private String code;

	@Column(nullable = false)
	private String name;

	private String platform;

	@Column(nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected PosDeviceEntity() {
	}

	public PosDeviceEntity(
		UUID id,
		UUID tenantId,
		UUID branchId,
		String code,
		String name,
		String platform,
		String status,
		Instant createdAt
	) {
		this.id = id;
		this.tenantId = tenantId;
		this.branchId = branchId;
		this.code = code;
		this.name = name;
		this.platform = platform;
		this.status = status;
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

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getPlatform() {
		return platform;
	}

	public String getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
