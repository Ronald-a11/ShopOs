package zw.miccs.shopos.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_sessions")
public class AuthSessionEntity {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "access_token_hash", nullable = false)
	private String accessTokenHash;

	@Column(name = "refresh_token_hash", nullable = false)
	private String refreshTokenHash;

	@Column(name = "remember_me", nullable = false)
	private boolean rememberMe;

	@Column(name = "access_expires_at", nullable = false)
	private Instant accessExpiresAt;

	@Column(name = "refresh_expires_at", nullable = false)
	private Instant refreshExpiresAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "last_used_at", nullable = false)
	private Instant lastUsedAt;

	@Column(name = "user_agent")
	private String userAgent;

	@Column(name = "ip_address")
	private String ipAddress;

	protected AuthSessionEntity() {
	}

	public AuthSessionEntity(
		UUID id,
		UUID userId,
		String accessTokenHash,
		String refreshTokenHash,
		boolean rememberMe,
		Instant accessExpiresAt,
		Instant refreshExpiresAt,
		Instant createdAt,
		Instant lastUsedAt,
		String userAgent,
		String ipAddress
	) {
		this.id = id;
		this.userId = userId;
		this.accessTokenHash = accessTokenHash;
		this.refreshTokenHash = refreshTokenHash;
		this.rememberMe = rememberMe;
		this.accessExpiresAt = accessExpiresAt;
		this.refreshExpiresAt = refreshExpiresAt;
		this.createdAt = createdAt;
		this.lastUsedAt = lastUsedAt;
		this.userAgent = userAgent;
		this.ipAddress = ipAddress;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getAccessTokenHash() {
		return accessTokenHash;
	}

	public String getRefreshTokenHash() {
		return refreshTokenHash;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public Instant getAccessExpiresAt() {
		return accessExpiresAt;
	}

	public Instant getRefreshExpiresAt() {
		return refreshExpiresAt;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public void setAccessTokenHash(String accessTokenHash) {
		this.accessTokenHash = accessTokenHash;
	}

	public void setRefreshTokenHash(String refreshTokenHash) {
		this.refreshTokenHash = refreshTokenHash;
	}

	public void setAccessExpiresAt(Instant accessExpiresAt) {
		this.accessExpiresAt = accessExpiresAt;
	}

	public void setRefreshExpiresAt(Instant refreshExpiresAt) {
		this.refreshExpiresAt = refreshExpiresAt;
	}

	public void setRevokedAt(Instant revokedAt) {
		this.revokedAt = revokedAt;
	}

	public void setLastUsedAt(Instant lastUsedAt) {
		this.lastUsedAt = lastUsedAt;
	}

}
