package zw.miccs.shopos.branches.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.domain.BranchStatus;
import zw.miccs.shopos.branches.domain.DeviceStatus;
import zw.miccs.shopos.branches.infrastructure.persistence.BranchEntity;
import zw.miccs.shopos.branches.infrastructure.persistence.BranchJpaRepository;
import zw.miccs.shopos.branches.infrastructure.persistence.PosDeviceEntity;
import zw.miccs.shopos.branches.infrastructure.persistence.PosDeviceJpaRepository;
import zw.miccs.shopos.common.ApiException;
import zw.miccs.shopos.common.Enums;

@Service
public class BranchService implements BranchQueryService {

	private final BranchJpaRepository branches;
	private final PosDeviceJpaRepository devices;

	public BranchService(BranchJpaRepository branches, PosDeviceJpaRepository devices) {
		this.branches = branches;
		this.devices = devices;
	}

	@Override
	@Transactional(readOnly = true)
	public BranchView requireBranch(UUID tenantId, UUID branchId) {
		return branches.findByTenantIdAndId(tenantId, branchId)
			.map(BranchService::toView)
			.orElseThrow(() -> notFound("BRANCH_NOT_FOUND", "Branch not found", "Branch does not exist"));
	}

	@Override
	@Transactional(readOnly = true)
	public List<BranchView> listBranches(UUID tenantId) {
		return branches.findByTenantIdOrderByCodeAsc(tenantId).stream()
			.map(BranchService::toView)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<DeviceView> listDevices(UUID tenantId, UUID branchId) {
		requireBranch(tenantId, branchId);
		return devices.findByTenantIdAndBranchIdOrderByCodeAsc(tenantId, branchId).stream()
			.map(BranchService::toView)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public DeviceView requireDevice(UUID tenantId, UUID deviceId) {
		return devices.findByTenantIdAndId(tenantId, deviceId)
			.map(BranchService::toView)
			.orElseThrow(() -> notFound("DEVICE_NOT_FOUND", "Device not found", "Device does not exist"));
	}

	@Transactional
	public BranchView create(UUID tenantId, String code, String name, String address) {
		String normalizedCode = required(code, "code");
		String normalizedName = required(name, "name");
		if (branches.findByTenantIdAndCode(tenantId, normalizedCode).isPresent()) {
			throw duplicateBranch();
		}
		BranchEntity entity = new BranchEntity(
			UUID.randomUUID(),
			tenantId,
			normalizedCode,
			normalizedName,
			blankToNull(address),
			BranchStatus.ACTIVE.name(),
			Instant.now());
		try {
			return toView(branches.saveAndFlush(entity));
		}
		catch (DataIntegrityViolationException ex) {
			throw duplicateBranch();
		}
	}

	@Transactional
	public BranchView update(UUID tenantId, UUID branchId, String name, String address, String status) {
		BranchEntity entity = branches.findByTenantIdAndId(tenantId, branchId)
			.orElseThrow(() -> notFound("BRANCH_NOT_FOUND", "Branch not found", "Branch does not exist"));
		if (name != null) {
			entity.setName(required(name, "name"));
		}
		if (address != null) {
			entity.setAddress(blankToNull(address));
		}
		if (status != null) {
			entity.setStatus(Enums.parse(BranchStatus.class, status, "status").name());
		}
		return toView(branches.save(entity));
	}

	@Transactional
	public DeviceView registerDevice(
		UUID tenantId,
		UUID branchId,
		String code,
		String name,
		String platform,
		String status
	) {
		requireBranch(tenantId, branchId);
		String normalizedCode = required(code, "code");
		String normalizedName = required(name, "name");
		DeviceStatus deviceStatus = status == null || status.isBlank()
			? DeviceStatus.AVAILABLE
			: Enums.parse(DeviceStatus.class, status, "status");
		if (devices.findByTenantIdAndCode(tenantId, normalizedCode).isPresent()) {
			throw duplicateDevice();
		}
		PosDeviceEntity entity = new PosDeviceEntity(
			UUID.randomUUID(),
			tenantId,
			branchId,
			normalizedCode,
			normalizedName,
			blankToNull(platform),
			deviceStatus.name(),
			Instant.now());
		try {
			return toView(devices.saveAndFlush(entity));
		}
		catch (DataIntegrityViolationException ex) {
			throw duplicateDevice();
		}
	}

	private static BranchView toView(BranchEntity entity) {
		return new BranchView(
			entity.getId(),
			entity.getTenantId(),
			entity.getCode(),
			entity.getName(),
			entity.getAddress(),
			entity.getStatus(),
			entity.getCreatedAt());
	}

	private static DeviceView toView(PosDeviceEntity entity) {
		return new DeviceView(
			entity.getId(),
			entity.getTenantId(),
			entity.getBranchId(),
			entity.getCode(),
			entity.getName(),
			entity.getPlatform(),
			entity.getStatus(),
			entity.getCreatedAt());
	}

	private static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				field + " must not be blank");
		}
		return value.trim();
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static ApiException notFound(String code, String title, String detail) {
		return new ApiException(HttpStatus.NOT_FOUND, code, title, detail);
	}

	private static ApiException duplicateBranch() {
		return new ApiException(HttpStatus.CONFLICT, "DUPLICATE_BRANCH_CODE", "Duplicate branch code",
			"Branch code already exists");
	}

	private static ApiException duplicateDevice() {
		return new ApiException(HttpStatus.CONFLICT, "DUPLICATE_DEVICE_CODE", "Duplicate device code",
			"Device code already exists");
	}

}
