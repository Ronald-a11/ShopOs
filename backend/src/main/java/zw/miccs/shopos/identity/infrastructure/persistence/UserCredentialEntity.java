package zw.miccs.shopos.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_credentials")
public class UserCredentialEntity {

	@Id
	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "password_updated_at", nullable = false)
	private Instant passwordUpdatedAt;

	protected UserCredentialEntity() {
	}

	public UserCredentialEntity(UUID userId, String passwordHash, Instant passwordUpdatedAt) {
		this.userId = userId;
		this.passwordHash = passwordHash;
		this.passwordUpdatedAt = passwordUpdatedAt;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void setPasswordUpdatedAt(Instant passwordUpdatedAt) {
		this.passwordUpdatedAt = passwordUpdatedAt;
	}

}
