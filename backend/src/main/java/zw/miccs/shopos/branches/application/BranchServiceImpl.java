package zw.miccs.shopos.branches.application;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.domain.Branch;
import zw.miccs.shopos.branches.domain.DevicePlatform;
import zw.miccs.shopos.branches.domain.DeviceStatus;
import zw.miccs.shopos.branches.domain.PosDevice;
import zw.miccs.shopos.branches.infrastructure.BranchEntity;
import zw.miccs.shopos.branches.infrastructure.BranchJpaRepository;
import zw.miccs.shopos.branches.infrastructure.PosDeviceEntity;
import zw.miccs.shopos.branches.infrastructure.PosDeviceJpaRepository;
import zw.miccs.shopos.common.api.ConflictException;
import zw.miccs.shopos.common.api.ForbiddenException;
import zw.miccs.shopos.common.api.NotFoundException;
import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.tenancy.application.TenantAccess;
import zw.miccs.shopos.tenancy.application.TenantContext;

@Service
@Transactional
public class BranchServiceImpl implements BranchService {

	private final BranchJpaRepository branches;
	private final PosDeviceJpaRepository devices;
	private final TenantAccess tenantAccess;

	public BranchServiceImpl(
		BranchJpaRepository branches,
		PosDeviceJpaRepository devices,
		TenantAccess tenantAccess) {
		this.branches = branches;
		this.devices = devices;
		this.tenantAccess = tenantAccess;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<Branch> list(Pageable pageable) {
		UUID tenantId = tenantAccess.requireTenantId();
		TenantContext.Holder holder = TenantContext.require();
		Page<BranchEntity> page;
		if (holder.allBranchesAccessible()) {
			page = branches.findByTenantId(tenantId, pageable);
		}
		else {
			page = branches.findByTenantIdAndIdIn(tenantId, holder.branchIdsOrEmpty(), pageable);
		}
		return PageResponse.from(page.map(BranchMapper::toDomain));
	}

	@Override
	public Branch create(String code, String name, String address) {
		requireManage();
		UUID tenantId = tenantAccess.requireTenantId();
		if (branches.existsByTenantIdAndCode(tenantId, code)) {
			throw new ConflictException("BRANCH_CODE_EXISTS", "Branch code already exists");
		}
		BranchEntity entity = new BranchEntity();
		entity.setTenantId(tenantId);
		entity.setCode(code.trim());
		entity.setName(name.trim());
		entity.setAddress(blankToNull(address));
		entity.setActive(true);
		try {
			return BranchMapper.toDomain(branches.save(entity));
		}
		catch (DataIntegrityViolationException ex) {
			throw new ConflictException("BRANCH_CODE_EXISTS", "Branch code already exists");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Branch get(UUID branchId) {
		return BranchMapper.toDomain(requireAccessibleBranch(branchId));
	}

	@Override
	public Branch update(UUID branchId, String name, String address, Boolean active) {
		requireManage();
		BranchEntity entity = requireAccessibleBranch(branchId);
		if (name != null) {
			entity.setName(name.trim());
		}
		if (address != null) {
			entity.setAddress(blankToNull(address));
		}
		if (active != null) {
			entity.setActive(active);
		}
		return BranchMapper.toDomain(branches.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PosDevice> listDevices(UUID branchId) {
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();
		return devices.findByTenantIdAndBranchIdOrderByNameAsc(tenantId, branchId).stream()
			.map(BranchMapper::toDomain)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public PosDevice getDevice(UUID branchId, UUID deviceId) {
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();
		PosDeviceEntity entity = devices.findByTenantIdAndBranchIdAndId(tenantId, branchId, deviceId)
			.orElseThrow(() -> new NotFoundException("DEVICE_NOT_FOUND", "POS device not found"));
		if (entity.getStatus() != DeviceStatus.ACTIVE) {
			throw new NotFoundException("DEVICE_NOT_FOUND", "POS device not found");
		}
		return BranchMapper.toDomain(entity);
	}

	@Override
	public PosDevice registerDevice(UUID branchId, String name, DevicePlatform platform, String externalSerial) {
		requireManage();
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();
		PosDeviceEntity entity = new PosDeviceEntity();
		entity.setTenantId(tenantId);
		entity.setBranchId(branchId);
		entity.setName(name.trim());
		entity.setPlatform(platform);
		entity.setExternalSerial(blankToNull(externalSerial));
		entity.setStatus(DeviceStatus.ACTIVE);
		return BranchMapper.toDomain(devices.save(entity));
	}

	private BranchEntity requireAccessibleBranch(UUID branchId) {
		tenantAccess.requireBranch(branchId);
		return branches.findByTenantIdAndId(tenantAccess.requireTenantId(), branchId)
			.orElseThrow(() -> new NotFoundException("BRANCH_NOT_FOUND", "Branch not found"));
	}

	private void requireManage() {
		if (!tenantAccess.canManageBranches()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to manage branches");
		}
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
