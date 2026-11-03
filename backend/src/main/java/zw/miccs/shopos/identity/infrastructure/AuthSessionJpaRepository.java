package zw.miccs.shopos.identity.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionEntity, UUID> {

	Optional<AuthSessionEntity> findByRefreshTokenHash(String refreshTokenHash);
}
