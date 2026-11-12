package zw.miccs.shopos.synchronisation.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class SyncDtos {

	private SyncDtos() {
	}

	public record BranchSnapshot(
		UUID id,
		String code,
		String name,
		String address,
		String status
	) {
	}

	public record DeviceSnapshot(
		UUID id,
		UUID branchId,
		String code,
		String name,
		String platform,
		String status
	) {
	}

	public record ProductSnapshot(
		UUID id,
		String sku,
		String name,
		String barcode,
		BigDecimal unitPrice,
		String taxRateCode,
		String status
	) {
	}

	public record BootstrapResponse(
		UUID tenantId,
		List<BranchSnapshot> branches,
		List<DeviceSnapshot> devices,
		List<ProductSnapshot> products,
		Instant generatedAt
	) {
	}

}
