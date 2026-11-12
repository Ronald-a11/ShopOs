package zw.miccs.shopos.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String email;

	@Column(name = "display_name", nullable = false)
	private String displayName;

	private String phone;

	@Column(nullable = false)
	private String status;

	@Column(name = "email_verified_at")
	private Instant emailVerifiedAt;

	@Column(name = "phone_verified_at")
	private Instant phoneVerifiedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected UserEntity() {
	}

	public UserEntity(
		UUID id,
		String email,
		String displayName,
		String phone,
		String status,
		Instant emailVerifiedAt,
		Instant phoneVerifiedAt,
		Instant createdAt,
		Instant updatedAt
	) {
		this.id = id;
		this.email = email;
		this.displayName = displayName;
		this.phone = phone;
		this.status = status;
		this.emailVerifiedAt = emailVerifiedAt;
		this.phoneVerifiedAt = phoneVerifiedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getPhone() {
		return phone;
	}

	public String getStatus() {
		return status;
	}

	public Instant getEmailVerifiedAt() {
		return emailVerifiedAt;
	}

	public Instant getPhoneVerifiedAt() {
		return phoneVerifiedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setEmailVerifiedAt(Instant emailVerifiedAt) {
		this.emailVerifiedAt = emailVerifiedAt;
	}

	public void setPhoneVerifiedAt(Instant phoneVerifiedAt) {
		this.phoneVerifiedAt = phoneVerifiedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

}
