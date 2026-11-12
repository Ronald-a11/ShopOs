package zw.miccs.shopos.branches.application;

import java.time.Instant;
import java.util.UUID;

public record DeviceView(
	UUID id,
	UUID tenantId,
	UUID branchId,
	String code,
	String name,
	String platform,
	String status,
	Instant createdAt
) {
}
