package zw.miccs.shopos.branches.application;

import java.time.Instant;
import java.util.UUID;

public record BranchView(
	UUID id,
	UUID tenantId,
	String code,
	String name,
	String address,
	String status,
	Instant createdAt
) {
}
