package zw.miccs.shopos.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "verification_challenges")
public class VerificationChallengeEntity {

	@Id
	private UUID id;

	@Column(name = "user_id")
	private UUID userId;

	@Column(nullable = false)
	private String method;

	@Column(nullable = false)
	private String destination;

	@Column(name = "code_hash", nullable = false)
	private String codeHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "consumed_at")
	private Instant consumedAt;

	@Column(nullable = false)
	private int attempts;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected VerificationChallengeEntity() {
	}

	public VerificationChallengeEntity(
		UUID id,
		UUID userId,
		String method,
		String destination,
		String codeHash,
		Instant expiresAt,
		Instant createdAt
	) {
		this.id = id;
		this.userId = userId;
		this.method = method;
		this.destination = destination;
		this.codeHash = codeHash;
		this.expiresAt = expiresAt;
		this.attempts = 0;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getMethod() {
		return method;
	}

	public String getDestination() {
		return destination;
	}

	public String getCodeHash() {
		return codeHash;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public Instant getConsumedAt() {
		return consumedAt;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setConsumedAt(Instant consumedAt) {
		this.consumedAt = consumedAt;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

}
