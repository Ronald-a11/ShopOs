package zw.miccs.shopos.branches.api;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import zw.miccs.shopos.branches.api.BranchDtos.BranchResponse;
import zw.miccs.shopos.branches.api.BranchDtos.CreateBranchRequest;
import zw.miccs.shopos.branches.api.BranchDtos.PosDeviceResponse;
import zw.miccs.shopos.branches.api.BranchDtos.RegisterDeviceRequest;
import zw.miccs.shopos.branches.api.BranchDtos.UpdateBranchRequest;
import zw.miccs.shopos.branches.application.BranchService;
import zw.miccs.shopos.branches.domain.Branch;
import zw.miccs.shopos.common.api.PageResponse;

@RestController
@RequestMapping("/api/v1/branches")
public class BranchController {

	private final BranchService branchService;

	public BranchController(BranchService branchService) {
		this.branchService = branchService;
	}

	@GetMapping
	PageResponse<BranchResponse> list(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort) {
		PageResponse<Branch> result = branchService.list(toPageable(page, size, sort));
		List<BranchResponse> content = result.content().stream().map(BranchResponse::from).toList();
		return new PageResponse<>(content, result.page());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	BranchResponse create(@Valid @RequestBody CreateBranchRequest request) {
		return BranchResponse.from(branchService.create(request.code(), request.name(), request.address()));
	}

	@GetMapping("/{branchId}")
	BranchResponse get(@PathVariable UUID branchId) {
		return BranchResponse.from(branchService.get(branchId));
	}

	@PatchMapping("/{branchId}")
	BranchResponse update(@PathVariable UUID branchId, @Valid @RequestBody UpdateBranchRequest request) {
		return BranchResponse.from(branchService.update(branchId, request.name(), request.address(), request.active()));
	}

	@GetMapping("/{branchId}/devices")
	List<PosDeviceResponse> listDevices(@PathVariable UUID branchId) {
		return branchService.listDevices(branchId).stream().map(PosDeviceResponse::from).toList();
	}

	@PostMapping("/{branchId}/devices")
	@ResponseStatus(HttpStatus.CREATED)
	PosDeviceResponse registerDevice(
		@PathVariable UUID branchId,
		@Valid @RequestBody RegisterDeviceRequest request) {
		return PosDeviceResponse.from(
			branchService.registerDevice(branchId, request.name(), request.platform(), request.externalSerial()));
	}

	private static Pageable toPageable(int page, int size, String sort) {
		int safeSize = Math.min(Math.max(size, 1), 200);
		int safePage = Math.max(page, 0);
		if (sort == null || sort.isBlank()) {
			return PageRequest.of(safePage, safeSize, Sort.by("code").ascending());
		}
		String[] parts = sort.split(",");
		String property = parts[0].trim();
		Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
			? Sort.Direction.DESC
			: Sort.Direction.ASC;
		return PageRequest.of(safePage, safeSize, Sort.by(direction, property));
	}
}
