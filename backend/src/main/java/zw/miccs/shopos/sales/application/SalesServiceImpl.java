package zw.miccs.shopos.sales.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.branches.application.BranchService;
import zw.miccs.shopos.branches.domain.Branch;
import zw.miccs.shopos.catalogue.application.CatalogueService;
import zw.miccs.shopos.catalogue.domain.Product;
import zw.miccs.shopos.common.api.ConflictException;
import zw.miccs.shopos.common.api.ForbiddenException;
import zw.miccs.shopos.common.api.NotFoundException;
import zw.miccs.shopos.common.api.PageResponse;
import zw.miccs.shopos.common.api.UnprocessableEntityException;
import zw.miccs.shopos.inventory.application.InventoryService;
import zw.miccs.shopos.inventory.domain.MovementType;
import zw.miccs.shopos.sales.domain.CommitSaleResult;
import zw.miccs.shopos.sales.domain.FiscalStatus;
import zw.miccs.shopos.sales.domain.PaymentMethod;
import zw.miccs.shopos.sales.domain.Sale;
import zw.miccs.shopos.sales.domain.SaleItem;
import zw.miccs.shopos.sales.domain.SalePayment;
import zw.miccs.shopos.sales.domain.SaleStatus;
import zw.miccs.shopos.sales.domain.SaleTotals;
import zw.miccs.shopos.sales.infrastructure.SaleEntity;
import zw.miccs.shopos.sales.infrastructure.SaleItemEntity;
import zw.miccs.shopos.sales.infrastructure.SaleItemJpaRepository;
import zw.miccs.shopos.sales.infrastructure.SaleJpaRepository;
import zw.miccs.shopos.sales.infrastructure.SaleOperationEntity;
import zw.miccs.shopos.sales.infrastructure.SaleOperationJpaRepository;
import zw.miccs.shopos.sales.infrastructure.SalePaymentEntity;
import zw.miccs.shopos.sales.infrastructure.SalePaymentJpaRepository;
import zw.miccs.shopos.sales.infrastructure.SaleReceiptSequenceEntity;
import zw.miccs.shopos.sales.infrastructure.SaleReceiptSequenceJpaRepository;
import zw.miccs.shopos.shifts.application.ShiftService;
import zw.miccs.shopos.shifts.domain.CashierShift;
import zw.miccs.shopos.tenancy.application.TenantAccess;
import zw.miccs.shopos.tenancy.application.TenantContext;
import zw.miccs.shopos.tenancy.domain.Role;

@Service
@Transactional
public class SalesServiceImpl implements SalesService {

	private static final BigDecimal VAT15 = new BigDecimal("0.15");
	private static final String REF_SALE = "SALE";
	private static final String REF_RETURN = "RETURN";
	private static final String REF_VOID = "VOID";

	private final SaleJpaRepository sales;
	private final SaleItemJpaRepository items;
	private final SalePaymentJpaRepository payments;
	private final SaleReceiptSequenceJpaRepository sequences;
	private final SaleOperationJpaRepository operations;
	private final CatalogueService catalogueService;
	private final BranchService branchService;
	private final InventoryService inventoryService;
	private final ShiftService shiftService;
	private final TenantAccess tenantAccess;

	public SalesServiceImpl(
		SaleJpaRepository sales,
		SaleItemJpaRepository items,
		SalePaymentJpaRepository payments,
		SaleReceiptSequenceJpaRepository sequences,
		SaleOperationJpaRepository operations,
		CatalogueService catalogueService,
		BranchService branchService,
		InventoryService inventoryService,
		ShiftService shiftService,
		TenantAccess tenantAccess) {
		this.sales = sales;
		this.items = items;
		this.payments = payments;
		this.sequences = sequences;
		this.operations = operations;
		this.catalogueService = catalogueService;
		this.branchService = branchService;
		this.inventoryService = inventoryService;
		this.shiftService = shiftService;
		this.tenantAccess = tenantAccess;
	}

	@Override
	public CommitSaleResult commitSale(
		UUID clientSaleId,
		UUID branchId,
		UUID deviceId,
		Instant occurredAt,
		int configVersion,
		String currency,
		List<SaleItemInput> itemInputs,
		List<SalePaymentInput> paymentInputs) {
		requireOperateRole();
		UUID tenantId = tenantAccess.requireTenantId();
		UUID cashierUserId = TenantContext.require().userId();

		if (clientSaleId == null) {
			throw new UnprocessableEntityException("CLIENT_SALE_ID_REQUIRED", "clientSaleId is required");
		}

		var existing = sales.findByTenantIdAndDeviceIdAndClientSaleId(tenantId, deviceId, clientSaleId);
		if (existing.isPresent()) {
			Sale sale = toDomain(existing.get());
			return new CommitSaleResult(sale, true, existing.get().getChangeAmount(), "APPLIED");
		}

		Branch branch = branchService.get(branchId);
		branchService.getDevice(branchId, deviceId);
		CashierShift shift = shiftService.requireOpenShift(branchId, deviceId);
		String normalizedCurrency = requireCurrency(currency);

		if (itemInputs == null || itemInputs.isEmpty()) {
			throw new UnprocessableEntityException("ITEMS_REQUIRED", "At least one sale item is required");
		}
		if (paymentInputs == null || paymentInputs.isEmpty()) {
			throw new UnprocessableEntityException("PAYMENTS_REQUIRED", "At least one payment is required");
		}

		List<BuiltLine> builtLines = buildSaleLines(itemInputs, false);
		SaleTotals totals = sumTotals(builtLines);
		BigDecimal paymentSum = validateAndSumPayments(paymentInputs, normalizedCurrency, totals.grandTotal());
		BigDecimal change = paymentSum.subtract(totals.grandTotal());

		String receiptNumber = nextReceiptNumber(tenantId, branch);

		SaleEntity sale = new SaleEntity();
		sale.setTenantId(tenantId);
		sale.setBranchId(branchId);
		sale.setDeviceId(deviceId);
		sale.setClientSaleId(clientSaleId);
		sale.setReceiptNumber(receiptNumber);
		sale.setStatus(SaleStatus.COMMITTED);
		sale.setCurrency(normalizedCurrency);
		sale.setOccurredAt(occurredAt == null ? Instant.now() : occurredAt);
		sale.setCashierUserId(cashierUserId);
		sale.setShiftId(shift.id());
		sale.setConfigVersion(configVersion);
		sale.setSubtotal(totals.subtotal());
		sale.setDiscountTotal(totals.discountTotal());
		sale.setTaxTotal(totals.taxTotal());
		sale.setGrandTotal(totals.grandTotal());
		sale.setChangeAmount(change);
		sale.setFiscalStatus(FiscalStatus.PENDING_SUBMISSION);
		SaleEntity saved = sales.save(sale);

		persistLines(tenantId, saved.getId(), builtLines);
		persistPayments(tenantId, saved.getId(), paymentInputs);

		for (BuiltLine line : builtLines) {
			inventoryService.applyStockChange(
				branchId,
				line.productId(),
				MovementType.SALE,
				line.quantity().negate(),
				REF_SALE,
				saved.getId());
		}

		BigDecimal cashTender = cashAmount(paymentInputs);
		if (cashTender.compareTo(BigDecimal.ZERO) > 0) {
			shiftService.applyCashTenderDelta(shift.id(), cashTender);
		}

		return new CommitSaleResult(toDomain(saved), false, change, "APPLIED");
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<Sale> search(
		UUID branchId,
		Instant from,
		Instant to,
		UUID cashierUserId,
		Pageable pageable) {
		requireOperateRole();
		UUID tenantId = tenantAccess.requireTenantId();
		Role role = TenantContext.require().role();
		UUID scopedBranch = branchId;
		if (scopedBranch == null) {
			if (!role.canReadAllBranchesWhenUnscoped()) {
				throw new ForbiddenException("FORBIDDEN", "branchId is required for this role");
			}
		}
		else {
			tenantAccess.requireBranch(scopedBranch);
		}
		Instant fromBound = from == null ? Instant.EPOCH : from;
		Instant toBound = to == null ? Instant.parse("9999-12-31T23:59:59Z") : to;
		Page<SaleEntity> page = sales.search(tenantId, scopedBranch, cashierUserId, fromBound, toBound, pageable);
		return PageResponse.from(page.map(this::toDomain));
	}

	@Override
	@Transactional(readOnly = true)
	public Sale get(UUID saleId) {
		requireOperateRole();
		SaleEntity entity = requireSale(tenantAccess.requireTenantId(), saleId);
		tenantAccess.requireBranch(entity.getBranchId());
		return toDomain(entity);
	}

	@Override
	public Sale initiateReturn(UUID saleId, UUID clientOperationId, String reason, List<ReturnLineInput> lines) {
		requireVarianceRole();
		UUID tenantId = tenantAccess.requireTenantId();

		if (clientOperationId != null) {
			var existingOp = operations.findByTenantIdAndClientOperationId(tenantId, clientOperationId);
			if (existingOp.isPresent()) {
				return toDomain(requireSale(tenantId, existingOp.get().getAggregateId()));
			}
		}

		if (reason == null || reason.isBlank()) {
			throw new UnprocessableEntityException("REASON_REQUIRED", "Return reason is required");
		}
		if (lines == null || lines.isEmpty()) {
			throw new UnprocessableEntityException("LINES_REQUIRED", "At least one return line is required");
		}

		SaleEntity original = requireSale(tenantId, saleId);
		tenantAccess.requireBranch(original.getBranchId());
		if (original.getStatus() == SaleStatus.VOIDED || original.getStatus() == SaleStatus.RETURNED) {
			throw new ConflictException("SALE_NOT_RETURNABLE", "Sale cannot be returned");
		}
		if (original.getOriginalSaleId() != null) {
			throw new ConflictException("SALE_NOT_RETURNABLE", "Cannot return a return document");
		}

		Map<UUID, BigDecimal> remaining = remainingReturnableQty(tenantId, original);
		List<BuiltLine> returnLines = new ArrayList<>();
		for (ReturnLineInput line : lines) {
			if (line.quantity() == null || line.quantity().compareTo(BigDecimal.ZERO) <= 0) {
				throw new UnprocessableEntityException("INVALID_QUANTITY", "Return quantity must be greater than zero");
			}
			BigDecimal available = remaining.getOrDefault(line.productId(), BigDecimal.ZERO);
			if (line.quantity().compareTo(available) > 0) {
				throw new UnprocessableEntityException("RETURN_EXCEEDS_SOLD", "Return quantity exceeds remaining sold quantity");
			}
			SaleItemEntity originalItem = items.findByTenantIdAndSaleIdOrderByCreatedAtAsc(tenantId, original.getId())
				.stream()
				.filter(i -> i.getProductId().equals(line.productId()))
				.findFirst()
				.orElseThrow(() -> new UnprocessableEntityException("PRODUCT_NOT_ON_SALE", "Product was not on the original sale"));

			BigDecimal ratio = line.quantity().divide(originalItem.getQuantity(), 8, RoundingMode.HALF_UP);
			BigDecimal discount = originalItem.getDiscount().multiply(ratio).setScale(4, RoundingMode.HALF_UP);
			BigDecimal net = line.quantity().multiply(originalItem.getUnitPrice()).subtract(discount)
				.setScale(4, RoundingMode.HALF_UP);
			BigDecimal tax = taxFor(originalItem.getTaxCode(), net);
			returnLines.add(new BuiltLine(
				originalItem.getProductId(),
				originalItem.getSku(),
				originalItem.getDescription(),
				line.quantity().negate(),
				originalItem.getUnitPrice(),
				originalItem.getTaxCode(),
				discount.negate(),
				tax.negate(),
				net.add(tax).negate()));
			remaining.put(line.productId(), available.subtract(line.quantity()));
		}

		SaleTotals totals = sumTotals(returnLines);
		CashierShift shift = shiftService.requireOpenShift(original.getBranchId(), original.getDeviceId());
		Branch branch = branchService.get(original.getBranchId());
		String receiptNumber = nextReceiptNumber(tenantId, branch);

		SaleEntity ret = new SaleEntity();
		ret.setTenantId(tenantId);
		ret.setBranchId(original.getBranchId());
		ret.setDeviceId(original.getDeviceId());
		ret.setReceiptNumber(receiptNumber);
		ret.setStatus(SaleStatus.RETURNED);
		ret.setCurrency(original.getCurrency());
		ret.setOccurredAt(Instant.now());
		ret.setCashierUserId(TenantContext.require().userId());
		ret.setShiftId(shift.id());
		ret.setOriginalSaleId(original.getId());
		ret.setConfigVersion(original.getConfigVersion());
		ret.setSubtotal(totals.subtotal());
		ret.setDiscountTotal(totals.discountTotal());
		ret.setTaxTotal(totals.taxTotal());
		ret.setGrandTotal(totals.grandTotal());
		ret.setFiscalStatus(FiscalStatus.PENDING_SUBMISSION);
		ret.setReason(reason.trim());
		SaleEntity savedReturn = sales.save(ret);
		persistLines(tenantId, savedReturn.getId(), returnLines);

		for (BuiltLine line : returnLines) {
			inventoryService.applyStockChange(
				original.getBranchId(),
				line.productId(),
				MovementType.RETURN,
				line.quantity().abs(),
				REF_RETURN,
				savedReturn.getId());
		}

		boolean anyRemaining = remaining.values().stream().anyMatch(q -> q.compareTo(BigDecimal.ZERO) > 0);
		original.setStatus(anyRemaining ? SaleStatus.PARTIALLY_RETURNED : SaleStatus.RETURNED);
		sales.save(original);

		saveOperation(tenantId, clientOperationId, SaleOperationEntity.TYPE_RETURN, savedReturn.getId());
		return toDomain(savedReturn);
	}

	@Override
	public Sale requestVoid(UUID saleId, UUID clientOperationId, String reason) {
		requireVarianceRole();
		UUID tenantId = tenantAccess.requireTenantId();

		if (clientOperationId != null) {
			var existingOp = operations.findByTenantIdAndClientOperationId(tenantId, clientOperationId);
			if (existingOp.isPresent()) {
				return toDomain(requireSale(tenantId, existingOp.get().getAggregateId()));
			}
		}

		if (reason == null || reason.isBlank()) {
			throw new UnprocessableEntityException("REASON_REQUIRED", "Void reason is required");
		}

		SaleEntity sale = requireSale(tenantId, saleId);
		tenantAccess.requireBranch(sale.getBranchId());
		if (sale.getStatus() != SaleStatus.COMMITTED) {
			throw new ConflictException("SALE_NOT_VOIDABLE", "Only committed sales without returns can be voided");
		}
		if (!sales.findByTenantIdAndOriginalSaleId(tenantId, sale.getId()).isEmpty()) {
			throw new ConflictException("SALE_NOT_VOIDABLE", "Sale has returns and cannot be voided");
		}

		List<SaleItemEntity> saleItems = items.findByTenantIdAndSaleIdOrderByCreatedAtAsc(tenantId, sale.getId());
		for (SaleItemEntity item : saleItems) {
			inventoryService.applyStockChange(
				sale.getBranchId(),
				item.getProductId(),
				MovementType.RETURN,
				item.getQuantity(),
				REF_VOID,
				sale.getId());
		}

		BigDecimal cashPaid = payments.findByTenantIdAndSaleIdOrderByCreatedAtAsc(tenantId, sale.getId()).stream()
			.filter(p -> p.getMethod() == PaymentMethod.CASH)
			.map(SalePaymentEntity::getAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		if (cashPaid.compareTo(BigDecimal.ZERO) > 0) {
			shiftService.applyCashTenderDelta(sale.getShiftId(), cashPaid.negate());
		}

		sale.setStatus(SaleStatus.VOIDED);
		sale.setReason(reason.trim());
		SaleEntity saved = sales.save(sale);
		saveOperation(tenantId, clientOperationId, SaleOperationEntity.TYPE_VOID, saved.getId());
		return toDomain(saved);
	}

	private Map<UUID, BigDecimal> remainingReturnableQty(UUID tenantId, SaleEntity original) {
		Map<UUID, BigDecimal> remaining = new HashMap<>();
		for (SaleItemEntity item : items.findByTenantIdAndSaleIdOrderByCreatedAtAsc(tenantId, original.getId())) {
			remaining.merge(item.getProductId(), item.getQuantity(), BigDecimal::add);
		}
		for (SaleEntity ret : sales.findByTenantIdAndOriginalSaleId(tenantId, original.getId())) {
			for (SaleItemEntity item : items.findByTenantIdAndSaleIdOrderByCreatedAtAsc(tenantId, ret.getId())) {
				remaining.merge(item.getProductId(), item.getQuantity().abs().negate(), BigDecimal::add);
			}
		}
		return remaining;
	}

	private List<BuiltLine> buildSaleLines(List<SaleItemInput> itemInputs, boolean negateMoney) {
		Role role = TenantContext.require().role();
		List<BuiltLine> built = new ArrayList<>();
		for (SaleItemInput input : itemInputs) {
			if (input.quantity() == null || input.quantity().compareTo(BigDecimal.ZERO) <= 0) {
				throw new UnprocessableEntityException("INVALID_QUANTITY", "Sale quantity must be greater than zero");
			}
			Product product = catalogueService.get(input.productId());
			if (!product.active()) {
				throw new UnprocessableEntityException("PRODUCT_INACTIVE", "Product is inactive");
			}
			if (product.unitPrice() == null) {
				throw new UnprocessableEntityException("PRODUCT_PRICE_MISSING", "Product has no unit price");
			}
			BigDecimal discount = input.approvedDiscount() == null ? BigDecimal.ZERO : input.approvedDiscount();
			if (discount.compareTo(BigDecimal.ZERO) < 0) {
				throw new UnprocessableEntityException("INVALID_DISCOUNT", "Discount must be zero or greater");
			}
			if (discount.compareTo(BigDecimal.ZERO) > 0 && !role.canApproveShiftVariance()) {
				throw new ForbiddenException("FORBIDDEN", "Insufficient role to apply approved discounts");
			}
			BigDecimal gross = input.quantity().multiply(product.unitPrice()).setScale(4, RoundingMode.HALF_UP);
			if (discount.compareTo(gross) > 0) {
				throw new UnprocessableEntityException("INVALID_DISCOUNT", "Discount exceeds line gross");
			}
			BigDecimal net = gross.subtract(discount);
			BigDecimal tax = taxFor(product.taxCode(), net);
			BigDecimal lineTotal = net.add(tax);
			if (negateMoney) {
				built.add(new BuiltLine(
					product.id(), product.sku(), product.name(),
					input.quantity().negate(), product.unitPrice(), product.taxCode(),
					discount.negate(), tax.negate(), lineTotal.negate()));
			}
			else {
				built.add(new BuiltLine(
					product.id(), product.sku(), product.name(),
					input.quantity(), product.unitPrice(), product.taxCode(),
					discount, tax, lineTotal));
			}
		}
		return built;
	}

	private static SaleTotals sumTotals(List<BuiltLine> lines) {
		BigDecimal subtotal = BigDecimal.ZERO;
		BigDecimal discountTotal = BigDecimal.ZERO;
		BigDecimal taxTotal = BigDecimal.ZERO;
		BigDecimal grandTotal = BigDecimal.ZERO;
		for (BuiltLine line : lines) {
			BigDecimal gross = line.quantity().multiply(line.unitPrice()).setScale(4, RoundingMode.HALF_UP);
			subtotal = subtotal.add(gross);
			discountTotal = discountTotal.add(line.discount());
			taxTotal = taxTotal.add(line.taxAmount());
			grandTotal = grandTotal.add(line.lineTotal());
		}
		return new SaleTotals(subtotal, discountTotal, taxTotal, grandTotal);
	}

	private BigDecimal validateAndSumPayments(
		List<SalePaymentInput> paymentInputs,
		String saleCurrency,
		BigDecimal grandTotal) {
		BigDecimal sum = BigDecimal.ZERO;
		for (SalePaymentInput payment : paymentInputs) {
			if (payment.method() == null) {
				throw new UnprocessableEntityException("INVALID_PAYMENT", "Payment method is required");
			}
			if (payment.amount() == null || payment.amount().compareTo(BigDecimal.ZERO) <= 0) {
				throw new UnprocessableEntityException("INVALID_PAYMENT", "Payment amount must be greater than zero");
			}
			String currency = requireCurrency(payment.currency());
			if (!saleCurrency.equals(currency)) {
				throw new UnprocessableEntityException("CURRENCY_MISMATCH", "Payment currency must match sale currency");
			}
			sum = sum.add(payment.amount());
		}
		if (sum.compareTo(grandTotal) < 0) {
			throw new UnprocessableEntityException("PAYMENT_SHORT", "Payments do not cover the sale total");
		}
		return sum;
	}

	private static BigDecimal cashAmount(List<SalePaymentInput> paymentInputs) {
		return paymentInputs.stream()
			.filter(p -> p.method() == PaymentMethod.CASH)
			.map(SalePaymentInput::amount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private String nextReceiptNumber(UUID tenantId, Branch branch) {
		SaleReceiptSequenceEntity seq = sequences.findForUpdate(tenantId, branch.id()).orElseGet(() -> {
			SaleReceiptSequenceEntity created = new SaleReceiptSequenceEntity();
			created.setTenantId(tenantId);
			created.setBranchId(branch.id());
			created.setNextValue(1L);
			return sequences.saveAndFlush(created);
		});
		// re-lock if just created without lock in some DBs
		seq = sequences.findForUpdate(tenantId, branch.id()).orElse(seq);
		long value = seq.getNextValue();
		seq.setNextValue(value + 1);
		sequences.save(seq);
		return branch.code() + "-" + String.format("%06d", value);
	}

	private void persistLines(UUID tenantId, UUID saleId, List<BuiltLine> builtLines) {
		for (BuiltLine line : builtLines) {
			SaleItemEntity entity = new SaleItemEntity();
			entity.setTenantId(tenantId);
			entity.setSaleId(saleId);
			entity.setProductId(line.productId());
			entity.setSku(line.sku());
			entity.setDescription(line.description());
			entity.setQuantity(line.quantity());
			entity.setUnitPrice(line.unitPrice());
			entity.setTaxCode(line.taxCode());
			entity.setDiscount(line.discount());
			entity.setTaxAmount(line.taxAmount());
			entity.setLineTotal(line.lineTotal());
			items.save(entity);
		}
	}

	private void persistPayments(UUID tenantId, UUID saleId, List<SalePaymentInput> paymentInputs) {
		for (SalePaymentInput payment : paymentInputs) {
			SalePaymentEntity entity = new SalePaymentEntity();
			entity.setTenantId(tenantId);
			entity.setSaleId(saleId);
			entity.setMethod(payment.method());
			entity.setCurrency(requireCurrency(payment.currency()));
			entity.setAmount(payment.amount());
			entity.setProviderReference(blankToNull(payment.providerReference()));
			entity.setTerminalId(blankToNull(payment.terminalId()));
			entity.setCardScheme(blankToNull(payment.cardScheme()));
			entity.setMaskedPanLast4(blankToNull(payment.maskedPanLast4()));
			payments.save(entity);
		}
	}

	private void saveOperation(UUID tenantId, UUID clientOperationId, String type, UUID aggregateId) {
		if (clientOperationId == null) {
			return;
		}
		SaleOperationEntity op = new SaleOperationEntity();
		op.setTenantId(tenantId);
		op.setClientOperationId(clientOperationId);
		op.setOperationType(type);
		op.setAggregateId(aggregateId);
		operations.save(op);
	}

	private SaleEntity requireSale(UUID tenantId, UUID saleId) {
		return sales.findByTenantIdAndId(tenantId, saleId)
			.orElseThrow(() -> new NotFoundException("SALE_NOT_FOUND", "Sale not found"));
	}

	private Sale toDomain(SaleEntity entity) {
		UUID tenantId = entity.getTenantId();
		List<SaleItem> saleItems = items.findByTenantIdAndSaleIdOrderByCreatedAtAsc(tenantId, entity.getId())
			.stream()
			.map(i -> new SaleItem(
				i.getProductId(),
				i.getSku(),
				i.getDescription(),
				i.getQuantity(),
				i.getUnitPrice(),
				i.getTaxCode(),
				i.getDiscount(),
				i.getLineTotal()))
			.toList();
		List<SalePayment> salePayments = payments.findByTenantIdAndSaleIdOrderByCreatedAtAsc(tenantId, entity.getId())
			.stream()
			.map(p -> new SalePayment(
				p.getMethod(),
				p.getCurrency(),
				p.getAmount(),
				p.getProviderReference(),
				p.getTerminalId(),
				p.getCardScheme(),
				p.getMaskedPanLast4()))
			.toList();
		return new Sale(
			entity.getId(),
			entity.getBranchId(),
			entity.getDeviceId(),
			entity.getClientSaleId(),
			entity.getReceiptNumber(),
			entity.getStatus(),
			entity.getCurrency(),
			entity.getOccurredAt(),
			entity.getCashierUserId(),
			entity.getShiftId(),
			entity.getOriginalSaleId(),
			new SaleTotals(
				entity.getSubtotal(),
				entity.getDiscountTotal(),
				entity.getTaxTotal(),
				entity.getGrandTotal()),
			saleItems,
			salePayments,
			entity.getFiscalStatus(),
			null);
	}

	private static BigDecimal taxFor(String taxCode, BigDecimal net) {
		if ("VAT15".equalsIgnoreCase(taxCode)) {
			return net.multiply(VAT15).setScale(4, RoundingMode.HALF_UP);
		}
		return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
	}

	private static void requireOperateRole() {
		if (!TenantContext.require().role().canOperateShift()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role to operate sales");
		}
	}

	private static void requireVarianceRole() {
		if (!TenantContext.require().role().canApproveShiftVariance()) {
			throw new ForbiddenException("FORBIDDEN", "Insufficient role for return or void");
		}
	}

	private static String requireCurrency(String currency) {
		if (currency == null || currency.isBlank() || currency.trim().length() != 3) {
			throw new UnprocessableEntityException("INVALID_CURRENCY", "currency must be a 3-letter ISO code");
		}
		return currency.trim().toUpperCase(Locale.ROOT);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private record BuiltLine(
		UUID productId,
		String sku,
		String description,
		BigDecimal quantity,
		BigDecimal unitPrice,
		String taxCode,
		BigDecimal discount,
		BigDecimal taxAmount,
		BigDecimal lineTotal
	) {
	}
}
