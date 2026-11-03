package zw.miccs.shopos.branches.api;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.miccs.shopos.branches.domain.Branch;
import zw.miccs.shopos.branches.domain.DevicePlatform;
import zw.miccs.shopos.branches.domain.DeviceStatus;
import zw.miccs.shopos.branches.domain.PosDevice;

public final class BranchDtos {

	private BranchDtos() {
	}

	public record CreateBranchRequest(
		@NotBlank String code,
		@NotBlank String name,
		String address
	) {
	}

	public record UpdateBranchRequest(
		String name,
		String address,
		Boolean active
	) {
	}

	public record RegisterDeviceRequest(
		@NotBlank String name,
		@NotNull DevicePlatform platform,
		String externalSerial
	) {
	}

	public record BranchResponse(
		UUID id,
		String code,
		String name,
		String address,
		boolean active
	) {
		static BranchResponse from(Branch branch) {
			return new BranchResponse(branch.id(), branch.code(), branch.name(), branch.address(), branch.active());
		}
	}

	public record PosDeviceResponse(
		UUID id,
		UUID branchId,
		String name,
		DevicePlatform platform,
		String externalSerial,
		DeviceStatus status
	) {
		static PosDeviceResponse from(PosDevice device) {
			return new PosDeviceResponse(
				device.id(),
				device.branchId(),
				device.name(),
				device.platform(),
				device.externalSerial(),
				device.status());
		}
	}
}
