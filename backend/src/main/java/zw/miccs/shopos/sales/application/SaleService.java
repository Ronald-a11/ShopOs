package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import zw.miccs.shopos.branches.application.BranchQueryService;
import zw.miccs.shopos.branches.application.BranchView;
import zw.miccs.shopos.branches.application.DeviceView;
import zw.miccs.shopos.branches.domain.BranchStatus;
import zw.miccs.shopos.catalogue.application.CatalogueQueryService;
import zw.miccs.shopos.common.ApiException;
import zw.miccs.shopos.fiscalisation.application.FiscalDocumentService;
import zw.miccs.shopos.fiscalisation.application.FiscalDocumentView;
import zw.miccs.shopos.inventory.application.InventoryService;
import zw.miccs.shopos.inventory.application.StockLine;
import zw.miccs.shopos.sales.domain.SaleStatus;
import zw.miccs.shopos.sales.infrastructure.persistence.SaleEntity;
import zw.miccs.shopos.sales.infrastructure.persistence.SaleItemEntity;
import zw.miccs.shopos.sales.infrastructure.persistence.SaleItemJpaRepository;
import zw.miccs.shopos.sales.infrastructure.persistence.SaleJpaRepository;
import zw.miccs.shopos.sales.infrastructure.persistence.SalePaymentEntity;
import zw.miccs.shopos.sales.infrastructure.persistence.SalePaymentJpaRepository;
import zw.miccs.shopos.shifts.application.ShiftQueryService;
import zw.miccs.shopos.shifts.application.ShiftView;
import zw.miccs.shopos.shifts.domain.ShiftStatus;

@Service
public class SaleService implements SalesQueryService {

	private final SaleJpaRepository sales;
	private final SaleItemJpaRepository items;
	private final SalePaymentJpaRepository payments;
	private final EntityManager entityManager;
	private final BranchQueryService branches;
	private final CatalogueQueryService catalogue;
	private final InventoryService inventory;
	private final FiscalDocumentService fiscalDocuments;
	private final ShiftQueryService shifts;

	public SaleService(
		SaleJpaRepository sales,
		SaleItemJpaRepository items,
		SalePaymentJpaRepository payments,
		EntityManager entityManager,
		BranchQueryService branches,
		CatalogueQueryService catalogue,
		InventoryService inventory,
		FiscalDocumentService fiscalDocuments,
		ShiftQueryService shifts
	) {
		this.sales = sales;
		this.items = items;
		this.payments = payments;
		this.entityManager = entityManager;
		this.branches = branches;
		this.catalogue = catalogue;
		this.inventory = inventory;
		this.fiscalDocuments = fiscalDocuments;
		this.shifts = shifts;
	}

	@Transactional(readOnly = true)
	public List<SaleSummaryView> list(UUID tenantId, UUID branchId) {
		List<SaleEntity> rows = branchId == null
			? sales.findByTenantIdOrderByCreatedAtDesc(tenantId)
			: listForBranch(tenantId, branchId);
		return rows.stream().map(SaleService::toSummary).toList();
	}

	@Transactional(readOnly = true)
	public SaleView get(UUID tenantId, UUID saleId) {
		return toView(require(tenantId, saleId));
	}

	@Transactional
	public SaleCommitResult commit(
		UUID tenantId,
		UUID userId,
		String clientSaleId,
		UUID branchId,
		UUID deviceId,
		UUID shiftId,
		String currency,
		List<SaleLineCommand> lines,
		List<SalePaymentCommand> paymentCommands
	) {
		String clientId = required(clientSaleId, "clientSaleId");
		BranchView branch = requireActiveBranch(tenantId, branchId);
		DeviceView device = branches.requireDevice(tenantId, deviceId);
		if (!device.branchId().equals(branchId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "DEVICE_BRANCH_MISMATCH", "Device branch mismatch",
				"Device does not belong to the branch");
		}
		ShiftView shift = shifts.requireShift(tenantId, shiftId);
		if (!ShiftStatus.OPEN.name().equals(shift.status())) {
			throw new ApiException(HttpStatus.CONFLICT, "SHIFT_NOT_OPEN", "Shift not open", "Shift is not open");
		}
		if (!shift.branchId().equals(branchId) || !shift.deviceId().equals(deviceId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "SHIFT_MISMATCH", "Shift mismatch",
				"Shift does not match the branch and device");
		}

		SaleEntity existing = sales.findByTenantIdAndDeviceIdAndClientSaleId(tenantId, deviceId, clientId).orElse(null);
		if (existing != null) {
			return replay(tenantId, existing);
		}

		if (lines == null || lines.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"lines must not be empty");
		}
		if (paymentCommands == null || paymentCommands.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"payments must not be empty");
		}
		String normalizedCurrency = required(currency, "currency").toUpperCase(Locale.ROOT);

		List<PricedLine> priced = new ArrayList<>();
		BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		for (SaleLineCommand line : lines) {
			catalogue.findActiveProduct(tenantId, line.productId());
			BigDecimal quantity = qty(line.quantity());
			if (quantity.signum() <= 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
					"quantity must be greater than zero");
			}
			if (line.unitPrice() == null || line.unitPrice().signum() < 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
					"unitPrice must be zero or greater");
			}
			BigDecimal unitPrice = money(line.unitPrice());
			BigDecimal lineTotal = money(unitPrice.multiply(quantity));
			priced.add(new PricedLine(line.productId(), quantity, unitPrice, lineTotal));
			total = total.add(lineTotal);
		}
		total = money(total);

		BigDecimal paid = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		List<SalePaymentCommand> normalizedPayments = new ArrayList<>();
		for (SalePaymentCommand payment : paymentCommands) {
			String method = required(payment.method(), "method");
			if (payment.amount() == null || payment.amount().signum() <= 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
					"payment amount must be greater than zero");
			}
			BigDecimal amount = money(payment.amount());
			paid = paid.add(amount);
			normalizedPayments.add(new SalePaymentCommand(method, amount, blankToNull(payment.providerReference())));
		}
		if (money(paid).compareTo(total) != 0) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "PAYMENT_MISMATCH", "Payment mismatch",
				"Payments must equal the sale total");
		}

		UUID saleId = UUID.randomUUID();
		Instant now = Instant.now();
		String receiptNumber = branch.code() + "-" + String.format("%06d", nextReceiptNumber(tenantId, branchId));
		SaleEntity sale = new SaleEntity(
			saleId,
			tenantId,
			branchId,
			deviceId,
			shiftId,
			userId,
			clientId,
			receiptNumber,
			normalizedCurrency,
			SaleStatus.COMPLETED.name(),
			total,
			now);
		sales.saveAndFlush(sale);

		for (PricedLine line : priced) {
			items.save(new SaleItemEntity(
				UUID.randomUUID(), saleId, tenantId, line.productId(), line.quantity(), line.unitPrice(), line.lineTotal()));
		}
		for (SalePaymentCommand payment : normalizedPayments) {
			payments.save(new SalePaymentEntity(
				UUID.randomUUID(),
				saleId,
				tenantId,
				payment.method(),
				payment.amount(),
				payment.providerReference()));
		}

		List<StockLine> stockLines = priced.stream()
			.map(line -> new StockLine(line.productId(), line.quantity()))
			.toList();
		inventory.applySale(tenantId, branchId, saleId, stockLines);
		FiscalDocumentView fiscal = fiscalDocuments.createPendingForSale(tenantId, saleId);
		return new SaleCommitResult(saleId, receiptNumber, "APPLIED", fiscal.status(), fiscal.documentId());
	}

	@Transactional
	public SaleView returnSale(UUID tenantId, UUID saleId) {
		SaleEntity sale = require(tenantId, saleId);
		if (SaleStatus.RETURNED.name().equals(sale.getStatus())) {
			return toView(sale);
		}
		if (!SaleStatus.COMPLETED.name().equals(sale.getStatus())) {
			throw new ApiException(HttpStatus.CONFLICT, "SALE_NOT_RETURNABLE", "Sale not returnable",
				"Only a completed sale can be returned");
		}
		List<SaleItemEntity> saleItems = items.findByTenantIdAndSaleId(tenantId, saleId);
		List<StockLine> stockLines = saleItems.stream()
			.map(item -> new StockLine(item.getProductId(), item.getQuantity()))
			.toList();
		inventory.applyReturn(tenantId, sale.getBranchId(), saleId, stockLines);
		sale.setStatus(SaleStatus.RETURNED.name());
		sales.save(sale);
		return toView(sale);
	}

	@Transactional
	public SaleSummaryView requestVoid(UUID tenantId, UUID saleId) {
		SaleEntity sale = require(tenantId, saleId);
		if (SaleStatus.VOID_REQUESTED.name().equals(sale.getStatus())) {
			return toSummary(sale);
		}
		if (!SaleStatus.COMPLETED.name().equals(sale.getStatus())) {
			throw new ApiException(HttpStatus.CONFLICT, "SALE_NOT_VOIDABLE", "Sale not voidable",
				"Only a completed sale can be flagged for void");
		}
		sale.setStatus(SaleStatus.VOID_REQUESTED.name());
		return toSummary(sales.save(sale));
	}

	@Override
	@Transactional(readOnly = true)
	public SalesReportView salesReport(UUID tenantId, UUID branchId, Instant from, Instant to) {
		if (from != null && to != null && from.isAfter(to)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"from must not be after to");
		}
		if (branchId != null) {
			branches.requireBranch(tenantId, branchId);
		}
		List<SaleEntity> rows = sales.findForReport(tenantId, branchId, from, to);
		BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		for (SaleEntity row : rows) {
			total = total.add(row.getTotalAmount());
		}
		return new SalesReportView(branchId, from, to, rows.size(), money(total));
	}

	private SaleCommitResult replay(UUID tenantId, SaleEntity existing) {
		FiscalDocumentView fiscal = fiscalDocuments.createPendingForSale(tenantId, existing.getId());
		return new SaleCommitResult(
			existing.getId(), existing.getReceiptNumber(), "APPLIED", fiscal.status(), fiscal.documentId());
	}

	private List<SaleEntity> listForBranch(UUID tenantId, UUID branchId) {
		branches.requireBranch(tenantId, branchId);
		return sales.findByTenantIdAndBranchIdOrderByCreatedAtDesc(tenantId, branchId);
	}

	private BranchView requireActiveBranch(UUID tenantId, UUID branchId) {
		BranchView branch = branches.requireBranch(tenantId, branchId);
		if (!BranchStatus.ACTIVE.name().equals(branch.status())) {
			throw new ApiException(HttpStatus.CONFLICT, "BRANCH_INACTIVE", "Branch inactive", "Branch is not active");
		}
		return branch;
	}

	private SaleEntity require(UUID tenantId, UUID saleId) {
		return sales.findByTenantIdAndId(tenantId, saleId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SALE_NOT_FOUND", "Sale not found",
				"Sale does not exist"));
	}

	private long nextReceiptNumber(UUID tenantId, UUID branchId) {
		Object result = entityManager.createNativeQuery("""
			INSERT INTO branch_receipt_sequences (tenant_id, branch_id, last_number)
			VALUES (:tenantId, :branchId, 1)
			ON CONFLICT (tenant_id, branch_id)
			DO UPDATE SET last_number = branch_receipt_sequences.last_number + 1
			RETURNING last_number
			""")
			.setParameter("tenantId", tenantId)
			.setParameter("branchId", branchId)
			.getSingleResult();
		if (result instanceof Number number) {
			return number.longValue();
		}
		if (result instanceof Object[] row && row.length > 0 && row[0] instanceof Number number) {
			return number.longValue();
		}
		throw new IllegalStateException("Unexpected receipt sequence result");
	}

	private SaleView toView(SaleEntity sale) {
		List<SaleLineView> lineViews = items.findByTenantIdAndSaleId(sale.getTenantId(), sale.getId()).stream()
			.map(item -> new SaleLineView(item.getProductId(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
			.toList();
		List<SalePaymentView> paymentViews = payments.findByTenantIdAndSaleId(sale.getTenantId(), sale.getId()).stream()
			.map(payment -> new SalePaymentView(payment.getMethod(), payment.getAmount(), payment.getProviderReference()))
			.toList();
		return new SaleView(
			sale.getId(),
			sale.getBranchId(),
			sale.getDeviceId(),
			sale.getShiftId(),
			sale.getClientSaleId(),
			sale.getReceiptNumber(),
			sale.getCurrency(),
			sale.getStatus(),
			sale.getTotalAmount(),
			sale.getCreatedAt(),
			lineViews,
			paymentViews);
	}

	private static SaleSummaryView toSummary(SaleEntity sale) {
		return new SaleSummaryView(
			sale.getId(),
			sale.getBranchId(),
			sale.getDeviceId(),
			sale.getReceiptNumber(),
			sale.getCurrency(),
			sale.getStatus(),
			sale.getTotalAmount(),
			sale.getCreatedAt());
	}

	private static BigDecimal money(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP);
	}

	private static BigDecimal qty(BigDecimal value) {
		if (value == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				"quantity is required");
		}
		return value.setScale(3, RoundingMode.HALF_UP);
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

	private record PricedLine(UUID productId, BigDecimal quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
	}

}
