package zw.miccs.shopos.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, UUID> {

	Optional<AuthSessionEntity> findByAccessTokenHashAndRevokedAtIsNull(String accessTokenHash);

	Optional<AuthSessionEntity> findByRefreshTokenHashAndRevokedAtIsNull(String refreshTokenHash);

	List<AuthSessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

}
