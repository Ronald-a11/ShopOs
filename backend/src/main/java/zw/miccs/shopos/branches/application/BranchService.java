package zw.miccs.shopos.branches.application;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import zw.miccs.shopos.branches.domain.Branch;
import zw.miccs.shopos.branches.domain.DevicePlatform;
import zw.miccs.shopos.branches.domain.PosDevice;
import zw.miccs.shopos.common.api.PageResponse;

public interface BranchService {

	PageResponse<Branch> list(Pageable pageable);

	Branch create(String code, String name, String address);

	Branch get(UUID branchId);

	Branch update(UUID branchId, String name, String address, Boolean active);

	List<PosDevice> listDevices(UUID branchId);

	PosDevice getDevice(UUID branchId, UUID deviceId);

	PosDevice registerDevice(UUID branchId, String name, DevicePlatform platform, String externalSerial);
}
