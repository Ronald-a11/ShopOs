package zw.miccs.shopos.identity.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Read-only projection of tenants for /me membership labels. */
@Entity
@Table(name = "tenants")
public class TenantNameEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
