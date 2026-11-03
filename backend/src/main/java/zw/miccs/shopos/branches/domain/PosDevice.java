package zw.miccs.shopos.branches.domain;

import java.util.UUID;

public record PosDevice(
	UUID id,
	UUID tenantId,
	UUID branchId,
	String name,
	DevicePlatform platform,
	String externalSerial,
	DeviceStatus status
) {
}
