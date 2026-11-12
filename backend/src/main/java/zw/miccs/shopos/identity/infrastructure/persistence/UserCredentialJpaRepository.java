package zw.miccs.shopos.identity.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCredentialJpaRepository extends JpaRepository<UserCredentialEntity, UUID> {
}
