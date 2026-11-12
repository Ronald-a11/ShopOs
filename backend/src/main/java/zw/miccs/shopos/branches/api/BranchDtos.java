package zw.miccs.shopos.branches.api;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class BranchDtos {

	private BranchDtos() {
	}

	public record CreateBranchRequest(
		@NotBlank @Size(max = 32) String code,
		@NotBlank @Size(max = 200) String name,
		@Size(max = 500) String address
	) {
	}

	public record UpdateBranchRequest(
		@Size(max = 200) String name,
		@Size(max = 500) String address,
		String status
	) {
	}

	public record RegisterDeviceRequest(
		@NotBlank @Size(max = 32) String code,
		@NotBlank @Size(max = 200) String name,
		@Size(max = 64) String platform
	) {
	}

	public record BranchResponse(
		UUID id,
		String code,
		String name,
		String address,
		String status,
		Instant createdAt
	) {
	}

	public record DeviceResponse(
		UUID id,
		UUID branchId,
		String code,
		String name,
		String platform,
		String status,
		Instant createdAt
	) {
	}

}
