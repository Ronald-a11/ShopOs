package zw.miccs.shopos.identity.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.common.api.UnauthorizedException;
import zw.miccs.shopos.identity.application.AuthModels.AuthResult;
import zw.miccs.shopos.identity.application.AuthModels.MeView;
import zw.miccs.shopos.identity.application.AuthModels.MembershipView;
import zw.miccs.shopos.identity.infrastructure.AuthSessionEntity;
import zw.miccs.shopos.identity.infrastructure.AuthSessionJpaRepository;
import zw.miccs.shopos.identity.infrastructure.JwtService;
import zw.miccs.shopos.identity.infrastructure.MembershipBranchJpaRepository;
import zw.miccs.shopos.identity.infrastructure.PosDeviceLookupEntity;
import zw.miccs.shopos.identity.infrastructure.PosDeviceLookupJpaRepository;
import zw.miccs.shopos.identity.infrastructure.RefreshTokenHasher;
import zw.miccs.shopos.identity.infrastructure.TenantMembershipEntity;
import zw.miccs.shopos.identity.infrastructure.TenantMembershipJpaRepository;
import zw.miccs.shopos.identity.infrastructure.TenantNameEntity;
import zw.miccs.shopos.identity.infrastructure.TenantNameJpaRepository;
import zw.miccs.shopos.identity.infrastructure.UserEntity;
import zw.miccs.shopos.identity.infrastructure.UserJpaRepository;
import zw.miccs.shopos.tenancy.domain.Role;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

	private final UserJpaRepository users;
	private final TenantMembershipJpaRepository memberships;
	private final MembershipBranchJpaRepository membershipBranches;
	private final TenantNameJpaRepository tenants;
	private final AuthSessionJpaRepository sessions;
	private final PosDeviceLookupJpaRepository devices;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenHasher refreshTokenHasher;
	private final AuthProperties authProperties;

	public AuthServiceImpl(
		UserJpaRepository users,
		TenantMembershipJpaRepository memberships,
		MembershipBranchJpaRepository membershipBranches,
		TenantNameJpaRepository tenants,
		AuthSessionJpaRepository sessions,
		PosDeviceLookupJpaRepository devices,
		PasswordEncoder passwordEncoder,
		JwtService jwtService,
		RefreshTokenHasher refreshTokenHasher,
		AuthProperties authProperties) {
		this.users = users;
		this.memberships = memberships;
		this.membershipBranches = membershipBranches;
		this.tenants = tenants;
		this.sessions = sessions;
		this.devices = devices;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.refreshTokenHasher = refreshTokenHasher;
		this.authProperties = authProperties;
	}

	@Override
	public AuthResult login(String email, String password, UUID deviceId, boolean keepSignedIn) {
		UserEntity user = users.findByEmailIgnoreCase(email.trim())
			.filter(u -> "ACTIVE".equals(u.getStatus()))
			.orElseThrow(this::badCredentials);
		if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
			throw badCredentials();
		}
		List<TenantMembershipEntity> userMemberships = memberships.findByUserIdOrderByCreatedAtAsc(user.getId());
		if (userMemberships.isEmpty()) {
			throw badCredentials();
		}
		TenantMembershipEntity active = selectMembership(userMemberships, deviceId);
		Set<UUID> branchIds = resolveBranchIds(active);
		ShoposPrincipal principal = new ShoposPrincipal(
			user.getId(), active.getTenantId(), user.getEmail(), active.getRole(), branchIds);
		return issueTokens(user, principal, deviceId, keepSignedIn);
	}

	@Override
	public AuthResult refresh(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new UnauthorizedException("UNAUTHORIZED", "Refresh token required");
		}
		String hash = refreshTokenHasher.hash(rawRefreshToken);
		AuthSessionEntity session = sessions.findByRefreshTokenHash(hash)
			.filter(s -> s.isActive(Instant.now()))
			.orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Invalid refresh token"));
		session.setRevokedAt(Instant.now());
		sessions.save(session);

		UserEntity user = users.findById(session.getUserId())
			.filter(u -> "ACTIVE".equals(u.getStatus()))
			.orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User disabled"));
		TenantMembershipEntity membership = memberships
			.findByTenantIdAndUserId(session.getTenantId(), session.getUserId())
			.orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Membership removed"));
		Set<UUID> branchIds = resolveBranchIds(membership);
		ShoposPrincipal principal = new ShoposPrincipal(
			user.getId(), membership.getTenantId(), user.getEmail(), membership.getRole(), branchIds);
		return issueTokens(user, principal, session.getDeviceId(), false);
	}

	@Override
	public void logout(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}
		sessions.findByRefreshTokenHash(refreshTokenHasher.hash(rawRefreshToken)).ifPresent(session -> {
			session.setRevokedAt(Instant.now());
			sessions.save(session);
		});
	}

	@Override
	@Transactional(readOnly = true)
	public MeView me(UUID userId) {
		UserEntity user = users.findById(userId)
			.orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "User not found"));
		return toMeView(user);
	}

	private AuthResult issueTokens(UserEntity user, ShoposPrincipal principal, UUID deviceId, boolean keepSignedIn) {
		String accessToken = jwtService.createAccessToken(principal);
		String rawRefresh = refreshTokenHasher.generateRawToken();
		AuthSessionEntity session = new AuthSessionEntity();
		session.setUserId(user.getId());
		session.setTenantId(principal.tenantId());
		session.setRefreshTokenHash(refreshTokenHasher.hash(rawRefresh));
		session.setDeviceId(deviceId);
		var ttl = keepSignedIn ? authProperties.getRefreshTokenTtlRemember() : authProperties.getRefreshTokenTtl();
		session.setExpiresAt(Instant.now().plus(ttl));
		sessions.save(session);
		return new AuthResult(accessToken, jwtService.accessTokenTtlSeconds(), rawRefresh, toMeView(user), principal);
	}

	private MeView toMeView(UserEntity user) {
		List<TenantMembershipEntity> list = memberships.findByUserIdOrderByCreatedAtAsc(user.getId());
		List<MembershipView> views = new ArrayList<>();
		for (TenantMembershipEntity m : list) {
			String tenantName = tenants.findById(m.getTenantId()).map(TenantNameEntity::getName).orElse(null);
			List<UUID> branchIds = membershipBranches.findByTenantIdAndUserId(m.getTenantId(), m.getUserId()).stream()
				.map(b -> b.getBranchId())
				.toList();
			views.add(new MembershipView(m.getTenantId(), tenantName, m.getRole(), branchIds));
		}
		return new MeView(user.getId(), user.getEmail(), user.getDisplayName(), views);
	}

	private TenantMembershipEntity selectMembership(List<TenantMembershipEntity> userMemberships, UUID deviceId) {
		if (deviceId != null) {
			PosDeviceLookupEntity device = devices.findActiveById(deviceId)
				.orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "Unknown or inactive device"));
			return userMemberships.stream()
				.filter(m -> m.getTenantId().equals(device.getTenantId()))
				.findFirst()
				.orElseThrow(() -> new UnauthorizedException("UNAUTHORIZED", "No membership for device tenant"));
		}
		return userMemberships.get(0);
	}

	private Set<UUID> resolveBranchIds(TenantMembershipEntity membership) {
		Role role = membership.getRole();
		List<UUID> assigned = membershipBranches
			.findByTenantIdAndUserId(membership.getTenantId(), membership.getUserId()).stream()
			.map(b -> b.getBranchId())
			.toList();
		if (assigned.isEmpty() && role.canReadAllBranchesWhenUnscoped()) {
			return null;
		}
		return assigned.stream().collect(Collectors.toUnmodifiableSet());
	}

	private UnauthorizedException badCredentials() {
		return new UnauthorizedException("UNAUTHORIZED", "Invalid email or password");
	}
}
