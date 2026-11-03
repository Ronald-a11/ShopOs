package zw.miccs.shopos.inventory.api;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.inventory.api.InventoryDtos.InventoryBalanceResponse;
import zw.miccs.shopos.inventory.application.InventoryService;
import zw.miccs.shopos.inventory.domain.InventoryBalance;

@RestController
@RequestMapping("/api/v1/branches/{branchId}/inventory")
public class BranchInventoryController {

	private final InventoryService inventoryService;

	public BranchInventoryController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@GetMapping
	PageResponse<InventoryBalanceResponse> listBranchInventory(
		@PathVariable UUID branchId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort) {
		PageResponse<InventoryBalance> result = inventoryService.listBalances(
			branchId,
			toPageable(page, size, sort));
		return new PageResponse<>(
			result.content().stream().map(InventoryBalanceResponse::from).toList(),
			result.page());
	}

	private static Pageable toPageable(int page, int size, String sort) {
		int safeSize = Math.min(Math.max(size, 1), 200);
		int safePage = Math.max(page, 0);
		if (sort == null || sort.isBlank()) {
			return PageRequest.of(safePage, safeSize, Sort.by("productId").ascending());
		}
		String[] parts = sort.split(",");
		String property = parts[0].trim();
		Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
			? Sort.Direction.DESC
			: Sort.Direction.ASC;
		return PageRequest.of(safePage, safeSize, Sort.by(direction, property));
	}
}
