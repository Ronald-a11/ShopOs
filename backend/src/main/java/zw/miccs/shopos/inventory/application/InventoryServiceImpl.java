package zw.miccs.shopos.inventory.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.application.BranchQueryService;
import zw.miccs.shopos.catalogue.application.CatalogueQueryService;
import zw.miccs.shopos.catalogue.application.ProductView;
import zw.miccs.shopos.common.ApiException;
import zw.miccs.shopos.inventory.domain.MovementType;
import zw.miccs.shopos.inventory.infrastructure.persistence.InventoryBalanceEntity;
import zw.miccs.shopos.inventory.infrastructure.persistence.InventoryBalanceId;
import zw.miccs.shopos.inventory.infrastructure.persistence.InventoryBalanceJpaRepository;
import zw.miccs.shopos.inventory.infrastructure.persistence.InventoryMovementEntity;
import zw.miccs.shopos.inventory.infrastructure.persistence.InventoryMovementJpaRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

	private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("5.000");

	private final InventoryBalanceJpaRepository balances;
	private final InventoryMovementJpaRepository movements;
	private final BranchQueryService branches;
	private final CatalogueQueryService catalogue;

	public InventoryServiceImpl(
		InventoryBalanceJpaRepository balances,
		InventoryMovementJpaRepository movements,
		BranchQueryService branches,
		CatalogueQueryService catalogue
	) {
		this.balances = balances;
		this.movements = movements;
		this.branches = branches;
		this.catalogue = catalogue;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BalanceView> listBalances(UUID tenantId, UUID branchId) {
		branches.requireBranch(tenantId, branchId);
		return balances.findByIdTenantIdAndIdBranchIdOrderByIdProductIdAsc(tenantId, branchId).stream()
			.map(row -> toBalance(tenantId, row))
			.toList();
	}

	@Override
	@Transactional
	public List<BalanceView> receive(UUID tenantId, UUID branchId, List<StockLine> lines, String note) {
		branches.requireBranch(tenantId, branchId);
		if (lines == null || lines.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"lines must not be empty");
		}
		List<BalanceView> updated = new ArrayList<>();
		for (StockLine line : lines) {
			BigDecimal quantity = qty(line.quantity());
			if (quantity.signum() <= 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
					"quantity must be greater than zero");
			}
			catalogue.findActiveProduct(tenantId, line.productId());
			updated.add(apply(tenantId, branchId, line.productId(), quantity, MovementType.RECEIPT, null,
				blankToNull(note), null));
		}
		return updated;
	}

	@Override
	@Transactional
	public BalanceView adjust(UUID tenantId, UUID branchId, UUID productId, BigDecimal quantityDelta, String reason) {
		branches.requireBranch(tenantId, branchId);
		catalogue.findActiveProduct(tenantId, productId);
		BigDecimal delta = qty(quantityDelta);
		if (delta.signum() == 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"quantityDelta must not be zero");
		}
		if (reason == null || reason.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"reason must not be blank");
		}
		return apply(tenantId, branchId, productId, delta, MovementType.ADJUSTMENT, reason.trim(), null, null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MovementView> listMovements(UUID tenantId, UUID branchId) {
		branches.requireBranch(tenantId, branchId);
		return movements.findByTenantIdAndBranchIdOrderByCreatedAtDesc(tenantId, branchId).stream()
			.map(InventoryServiceImpl::toMovement)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<BalanceView> listLowStock(UUID tenantId, UUID branchId, BigDecimal threshold) {
		if (branchId != null) {
			branches.requireBranch(tenantId, branchId);
		}
		BigDecimal limit = threshold == null ? DEFAULT_THRESHOLD : qty(threshold);
		return balances.findLowStock(tenantId, branchId, limit).stream()
			.map(row -> toBalance(tenantId, row))
			.toList();
	}

	@Override
	@Transactional
	public void applySale(UUID tenantId, UUID branchId, UUID saleId, List<StockLine> lines) {
		applySigned(tenantId, branchId, saleId, lines, MovementType.SALE, true);
	}

	@Override
	@Transactional
	public void applyReturn(UUID tenantId, UUID branchId, UUID saleId, List<StockLine> lines) {
		applySigned(tenantId, branchId, saleId, lines, MovementType.RETURN, false);
	}

	private void applySigned(
		UUID tenantId,
		UUID branchId,
		UUID saleId,
		List<StockLine> lines,
		MovementType type,
		boolean deduct
	) {
		branches.requireBranch(tenantId, branchId);
		if (lines == null || lines.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"lines must not be empty");
		}
		for (StockLine line : lines) {
			BigDecimal quantity = qty(line.quantity());
			if (quantity.signum() <= 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
					"quantity must be greater than zero");
			}
			if (catalogue.findProduct(tenantId, line.productId()).isEmpty()) {
				throw new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found",
					"Product does not exist");
			}
			if (deduct) {
				catalogue.findActiveProduct(tenantId, line.productId());
				ensureSufficient(tenantId, branchId, line.productId(), quantity);
			}
			BigDecimal delta = deduct ? quantity.negate() : quantity;
			apply(tenantId, branchId, line.productId(), delta, type, type.name(), null, saleId);
		}
	}

	private void ensureSufficient(UUID tenantId, UUID branchId, UUID productId, BigDecimal needed) {
		InventoryBalanceEntity balance = balances.findById(new InventoryBalanceId(tenantId, branchId, productId))
			.orElse(null);
		BigDecimal onHand = balance == null ? BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP) : balance.getQuantity();
		if (onHand.compareTo(needed) < 0) {
			throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "Insufficient stock",
				"Not enough stock for product " + productId);
		}
	}

	private BalanceView apply(
		UUID tenantId,
		UUID branchId,
		UUID productId,
		BigDecimal delta,
		MovementType type,
		String reason,
		String note,
		UUID referenceId
	) {
		Instant now = Instant.now();
		InventoryBalanceId id = new InventoryBalanceId(tenantId, branchId, productId);
		InventoryBalanceEntity balance = balances.findById(id).orElseGet(() -> balances.save(
			new InventoryBalanceEntity(id, BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP), now)));
		BigDecimal next = balance.getQuantity().add(delta).setScale(3, RoundingMode.HALF_UP);
		if (type == MovementType.SALE && next.signum() < 0) {
			throw new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "Insufficient stock",
				"Not enough stock for product " + productId);
		}
		balance.setQuantity(next);
		balance.setUpdatedAt(now);
		balances.save(balance);
		movements.save(new InventoryMovementEntity(
			UUID.randomUUID(),
			tenantId,
			branchId,
			productId,
			delta,
			type.name(),
			reason,
			note,
			referenceId,
			now));
		return toBalance(tenantId, balance);
	}

	private BalanceView toBalance(UUID tenantId, InventoryBalanceEntity entity) {
		ProductView product = catalogue.findProduct(tenantId, entity.getId().getProductId()).orElse(null);
		return new BalanceView(
			entity.getId().getBranchId(),
			entity.getId().getProductId(),
			product == null ? null : product.sku(),
			product == null ? null : product.name(),
			entity.getQuantity(),
			entity.getUpdatedAt());
	}

	private static MovementView toMovement(InventoryMovementEntity entity) {
		return new MovementView(
			entity.getId(),
			entity.getBranchId(),
			entity.getProductId(),
			entity.getQuantityDelta(),
			entity.getMovementType(),
			entity.getReason(),
			entity.getNote(),
			entity.getReferenceId(),
			entity.getCreatedAt());
	}

	private static BigDecimal qty(BigDecimal value) {
		if (value == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"quantity is required");
		}
		return value.setScale(3, RoundingMode.HALF_UP);
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

}
