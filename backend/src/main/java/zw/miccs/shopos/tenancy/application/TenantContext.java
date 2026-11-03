package zw.miccs.shopos.tenancy.application;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import zw.miccs.shopos.tenancy.domain.Role;

/**
 * Request-scoped tenant identity. Populated after authentication; cleared at request end.
 */
public final class TenantContext {

	private static final ThreadLocal<Holder> CURRENT = new ThreadLocal<>();

	private TenantContext() {
	}

	public static void set(UUID tenantId, UUID userId, Role role, Set<UUID> accessibleBranchIds) {
		CURRENT.set(new Holder(
			tenantId,
			userId,
			role,
			accessibleBranchIds == null ? null : Set.copyOf(accessibleBranchIds)));
	}

	public static Holder require() {
		Holder holder = CURRENT.get();
		if (holder == null) {
			throw new IllegalStateException("No tenant context on this thread");
		}
		return holder;
	}

	public static UUID requireTenantId() {
		return require().tenantId();
	}

	public static void clear() {
		CURRENT.remove();
	}

	public record Holder(UUID tenantId, UUID userId, Role role, Set<UUID> accessibleBranchIds) {

		/**
		 * {@code null} accessibleBranchIds means "all branches in tenant" for elevated roles.
		 * An empty set means no branch access.
		 */
		public boolean allBranchesAccessible() {
			return accessibleBranchIds == null;
		}

		public Set<UUID> branchIdsOrEmpty() {
			return accessibleBranchIds == null ? Collections.emptySet() : accessibleBranchIds;
		}
	}
}
