package zw.miccs.shopos.branches.application;

import zw.miccs.shopos.branches.domain.Branch;
import zw.miccs.shopos.branches.domain.PosDevice;
import zw.miccs.shopos.branches.infrastructure.BranchEntity;
import zw.miccs.shopos.branches.infrastructure.PosDeviceEntity;

final class BranchMapper {

	private BranchMapper() {
	}

	static Branch toDomain(BranchEntity entity) {
		return new Branch(
			entity.getId(),
			entity.getTenantId(),
			entity.getCode(),
			entity.getName(),
			entity.getAddress(),
			entity.isActive());
	}

	static PosDevice toDomain(PosDeviceEntity entity) {
		return new PosDevice(
			entity.getId(),
			entity.getTenantId(),
			entity.getBranchId(),
			entity.getName(),
			entity.getPlatform(),
			entity.getExternalSerial(),
			entity.getStatus());
	}
}
