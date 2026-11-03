package zw.miccs.shopos.inventory.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.application.BranchService;
import zw.miccs.shopos.catalogue.application.CatalogueService;
import zw.miccs.shopos.catalogue.domain.Product;
import zw.miccs.shopos.common.api.ForbiddenException;
import zw.miccs.shopos.common.api.NotFoundException;
import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.common.api.UnprocessableEntityException;
import zw.miccs.shopos.inventory.domain.InventoryAdjustmentResult;
import zw.miccs.shopos.inventory.domain.InventoryBalance;
import zw.miccs.shopos.inventory.domain.InventoryMovement;
import zw.miccs.shopos.inventory.domain.InventoryReceiptResult;
import zw.miccs.shopos.inventory.domain.MovementType;
import zw.miccs.shopos.inventory.infrastructure.InventoryBalanceEntity;
import zw.miccs.shopos.inventory.infrastructure.InventoryBalanceJpaRepository;
import zw.miccs.shopos.inventory.infrastructure.InventoryMovementEntity;
import zw.miccs.shopos.inventory.infrastructure.InventoryMovementJpaRepository;
import zw.miccs.shopos.inventory.infrastructure.InventoryOperationEntity;
import zw.miccs.shopos.inventory.infrastructure.InventoryOperationJpaRepository;
import zw.miccs.shopos.tenancy.application.TenantAccess;
import zw.miccs.shopos.tenancy.application.TenantContext;
import zw.miccs.shopos.tenancy.domain.Role;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

	private static final String REF_RECEIPT = "RECEIPT";
	private static final String REF_ADJUSTMENT = "ADJUSTMENT";

	private final InventoryBalanceJpaRepository balances;
	private final InventoryMovementJpaRepository movements;
	private final InventoryOperationJpaRepository operations;
	private final CatalogueService catalogueService;
	private final BranchService branchService;
	private final TenantAccess tenantAccess;

	public InventoryServiceImpl(
		InventoryBalanceJpaRepository balances,
		InventoryMovementJpaRepository movements,
		InventoryOperationJpaRepository operations,
		CatalogueService catalogueService,
		BranchService branchService,
		TenantAccess tenantAccess) {
		this.balances = balances;
		this.movements = movements;
		this.operations = operations;
		this.catalogueService = catalogueService;
		this.branchService = branchService;
		this.tenantAccess = tenantAccess;
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<InventoryBalance> listBalances(UUID branchId, Pageable pageable) {
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();
		Page<InventoryBalanceEntity> page = balances.findByTenantIdAndBranchId(tenantId, branchId, pageable);
		return PageResponse.from(page.map(this::toBalance));
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<InventoryMovement> listMovements(UUID branchId, UUID productId, Pageable pageable) {
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();
		Page<InventoryMovementEntity> page = movements.findByBranch(tenantId, branchId, productId, pageable);
		return PageResponse.from(page.map(this::toMovement));
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<InventoryBalance> listLowStock(UUID branchId, Pageable pageable) {
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();
		Page<InventoryBalanceEntity> page = balances.findLowStock(tenantId, branchId, pageable);
		return PageResponse.from(page.map(this::toBalance));
	}

	@Override
	public InventoryReceiptResult receiveGoods(
		UUID branchId,
		UUID clientOperationId,
		String reason,
		List<ReceiptLineInput> lines) {
		requireReceiveRole();
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();

		var existing = operations.findByTenantIdAndClientOperationId(tenantId, clientOperationId);
		if (existing.isPresent()) {
			InventoryOperationEntity op = existing.get();
			List<InventoryMovement> replayed = loadMovements(tenantId, REF_RECEIPT, op.getAggregateId());
			return new InventoryReceiptResult(op.getAggregateId(), op.getBranchId(), replayed, true);
		}

		if (lines == null || lines.isEmpty()) {
			throw new UnprocessableEntityException("INVALID_RECEIPT", "At least one receipt line is required");
		}

		UUID receiptId = UUID.randomUUID();
		List<InventoryMovement> created = new ArrayList<>();
		for (ReceiptLineInput line : lines) {
			if (line.quantity() == null || line.quantity().compareTo(BigDecimal.ZERO) <= 0) {
				throw new UnprocessableEntityException("INVALID_QUANTITY", "Receipt quantity must be greater than zero");
			}
			requireProduct(line.productId());
			created.add(applyStockChange(
				branchId,
				line.productId(),
				MovementType.RECEIPT,
				line.quantity(),
				REF_RECEIPT,
				receiptId));
		}

		saveOperation(tenantId, clientOperationId, InventoryOperationEntity.TYPE_RECEIPT, receiptId, branchId);
		return new InventoryReceiptResult(receiptId, branchId, created, false);
	}

	@Override
	public InventoryAdjustmentResult adjustStock(
		UUID branchId,
		UUID clientOperationId,
		String reason,
		MovementType movementType,
		List<AdjustmentLineInput> lines) {
		MovementType type = movementType == null ? MovementType.ADJUSTMENT : movementType;
		if (type != MovementType.ADJUSTMENT && type != MovementType.WASTAGE) {
			throw new UnprocessableEntityException("INVALID_MOVEMENT_TYPE", "Adjustment type must be ADJUSTMENT or WASTAGE");
		}
		if (type == MovementType.WASTAGE) {
			requireWastageRole();
		}
		else {
			requireAdjustRole();
		}
		if (reason == null || reason.isBlank()) {
			throw new UnprocessableEntityException("REASON_REQUIRED", "Adjustment reason is required");
		}
		requireAccessibleBranch(branchId);
		UUID tenantId = tenantAccess.requireTenantId();

		var existing = operations.findByTenantIdAndClientOperationId(tenantId, clientOperationId);
		if (existing.isPresent()) {
			InventoryOperationEntity op = existing.get();
			List<InventoryMovement> replayed = loadMovements(tenantId, REF_ADJUSTMENT, op.getAggregateId());
			return new InventoryAdjustmentResult(op.getAggregateId(), op.getBranchId(), replayed, true);
		}

		if (lines == null || lines.isEmpty()) {
			throw new UnprocessableEntityException("INVALID_ADJUSTMENT", "At least one adjustment line is required");
		}

		UUID adjustmentId = UUID.randomUUID();
		List<InventoryMovement> created = new ArrayList<>();
		for (AdjustmentLineInput line : lines) {
			if (line.quantityDelta() == null || line.quantityDelta().compareTo(BigDecimal.ZERO) == 0) {
				throw new UnprocessableEntityException("INVALID_QUANTITY", "Adjustment quantityDelta must be non-zero");
			}
			if (type == MovementType.WASTAGE && line.quantityDelta().compareTo(BigDecimal.ZERO) >= 0) {
				throw new UnprocessableEntityException("INVALID_WASTAGE", "Wastage quantityDelta must be negative");
			}
			requireProduct(line.productId());
			created.add(applyStockChange(
				branchId,
				line.productId(),
				type,
				line.quantityDelta(),
				REF_ADJUSTMENT,
				adjustmentId));
		}

		saveOperation(tenantId, clientOperationId, InventoryOperationEntity.TYPE_ADJUSTMENT, adjustmentId, branchId);
		return new InventoryAdjustmentResult(adjustmentId, branchId, created, false);
	}

	@Override
	public InventoryMovement applyStockChange(
		UUID branchId,
		UUID productId,
		MovementType movementType,
		BigDecimal quantityDelta,
		String referenceType,
		UUID referenceId) {
		if (quantityDelta == null || quantityDelta.compareTo(BigDecimal.ZERO) == 0) {
			throw new UnprocessableEntityException("INVALID_QUANTITY", "quantityDelta must be non-zero");
		}
		UUID tenantId = tenantAccess.requireTenantId();

		InventoryBalanceEntity balance = balances.findForUpdate(tenantId, branchId, productId)
			.orElseGet(() -> {
				InventoryBalanceEntity created = new InventoryBalanceEntity();
				created.setTenantId(tenantId);
				created.setBranchId(branchId);
				created.setProductId(productId);
				created.setQuantity(BigDecimal.ZERO);
				return created;
			});
		balance.setQuantity(balance.getQuantity().add(quantityDelta));
		balances.save(balance);

		InventoryMovementEntity movement = new InventoryMovementEntity();
		movement.setTenantId(tenantId);
		movement.setBranchId(branchId);
		movement.setProductId(productId);
		movement.setMovementType(movementType);
		movement.setQuantityDelta(quantityDelta);
		movement.setReferenceType(referenceType);
		movement.setReferenceId(referenceId);
		InventoryMovementEntity saved = movements.save(movement);
		return toMovement(saved);
	}

	private void saveOperation(
		UUID tenantId,
		UUID clientOperationId,
		String operationType,
		UUID aggregateId,
		UUID branchId) {
		InventoryOperationEntity op = new InventoryOperationEntity();
		op.setTenantId(tenantId);
		op.setClientOperationId(clientOperationId);
		op.setOperationType(operationType);
		op.setAggregateId(aggregateId);
		op.setBranchId(branchId);
		operations.save(op);
	}

	private List<InventoryMovement> loadMovements(UUID tenantId, String referenceType, UUID referenceId) {
		return movements
			.findByTenantIdAndReferenceTypeAndReferenceIdOrderByOccurredAtAsc(tenantId, referenceType, referenceId)
			.stream()
			.map(this::toMovement)
			.toList();
	}

	private void requireAccessibleBranch(UUID branchId) {
		branchService.get(branchId);
	}

	private Product requireProduct(UUID productId) {
		try {
			return catalogueService.get(productId);
		}
		catch (NotFoundException ex) {
			throw new NotFoundException("PRODUCT_NOT_FOUND", "Product not found");
		}
	}

	private InventoryBalance toBalance(InventoryBalanceEntity entity) {
		Product product = catalogueService.get(entity.getProductId());
		return new InventoryBalance(
			entity.getBranchId(),
			entity.getProductId(),
			product.sku(),
			product.name(),
			entity.getQuantity(),
			entity.getReorderLevel());
	}

	private InventoryMovement toMovement(InventoryMovementEntity entity) {
		return new InventoryMovement(
			entity.getId(),
			entity.getBranchId(),
			entity.getProductId(),
			entity.getMovementType(),
			entity.getQuantityDelta(),
			entity.getReferenceType(),
			entity.getReferenceId(),
			entity.getOccurredAt());
	}

	private static void requireReceiveRole() {
		Role role = TenantContext.require().role();
		if (!role.canReceiveStock()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to receive stock");
		}
	}

	private static void requireAdjustRole() {
		Role role = TenantContext.require().role();
		if (!role.canAdjustStock()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to adjust stock");
		}
	}

	private static void requireWastageRole() {
		Role role = TenantContext.require().role();
		if (!role.canRecordWastage()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to record wastage");
		}
	}
}
