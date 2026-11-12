package zw.miccs.shopos.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationChallengeJpaRepository extends JpaRepository<VerificationChallengeEntity, UUID> {

	Optional<VerificationChallengeEntity> findFirstByMethodAndDestinationAndConsumedAtIsNullOrderByCreatedAtDesc(
		String method,
		String destination
	);

}
