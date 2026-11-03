package zw.miccs.shopos.identity.infrastructure;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import zw.miccs.shopos.identity.application.AuthProperties;
import zw.miccs.shopos.identity.application.ShoposPrincipal;
import zw.miccs.shopos.tenancy.domain.Role;

@Component
public class JwtService {

	private final AuthProperties properties;
	private final SecretKey key;

	public JwtService(AuthProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(padSecret(properties.getJwtSecret()).getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(ShoposPrincipal principal) {
		Instant now = Instant.now();
		Instant exp = now.plus(properties.getAccessTokenTtl());
		var builder = Jwts.builder()
			.subject(principal.userId().toString())
			.claim("tid", principal.tenantId().toString())
			.claim("role", principal.role().name())
			.claim("email", principal.email())
			.issuedAt(Date.from(now))
			.expiration(Date.from(exp))
			.signWith(key);
		if (principal.accessibleBranchIds() != null) {
			builder.claim("bids", principal.accessibleBranchIds().stream().map(UUID::toString).toList());
		}
		return builder.compact();
	}

	public long accessTokenTtlSeconds() {
		return properties.getAccessTokenTtl().toSeconds();
	}

	public ShoposPrincipal parseAccessToken(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
		UUID userId = UUID.fromString(claims.getSubject());
		UUID tenantId = UUID.fromString(claims.get("tid", String.class));
		Role role = Role.valueOf(claims.get("role", String.class));
		String email = claims.get("email", String.class);
		Set<UUID> branchIds = null;
		Object bids = claims.get("bids");
		if (bids instanceof List<?> list) {
			branchIds = list.stream()
				.map(Object::toString)
				.map(UUID::fromString)
				.collect(Collectors.toUnmodifiableSet());
		}
		return new ShoposPrincipal(userId, tenantId, email, role, branchIds);
	}

	private static String padSecret(String secret) {
		if (secret.length() >= 32) {
			return secret;
		}
		return (secret + "0123456789abcdef0123456789abcdef").substring(0, 32);
	}
}
