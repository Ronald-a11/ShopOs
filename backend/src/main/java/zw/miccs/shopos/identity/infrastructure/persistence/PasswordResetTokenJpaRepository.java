package zw.miccs.shopos.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

	Optional<PasswordResetTokenEntity> findByTokenHashAndUsedAtIsNull(String tokenHash);

}
