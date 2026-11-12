package zw.miccs.shopos.fiscalisation.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zw.miccs.shopos.common.ApiException;
import zw.miccs.shopos.fiscalisation.domain.FiscalDayStatus;
import zw.miccs.shopos.fiscalisation.domain.FiscalDeviceStatus;
import zw.miccs.shopos.fiscalisation.domain.FiscalDocumentStatus;
import zw.miccs.shopos.fiscalisation.infrastructure.persistence.FiscalDaySessionEntity;
import zw.miccs.shopos.fiscalisation.infrastructure.persistence.FiscalDaySessionJpaRepository;
import zw.miccs.shopos.fiscalisation.infrastructure.persistence.FiscalDeviceEntity;
import zw.miccs.shopos.fiscalisation.infrastructure.persistence.FiscalDeviceJpaRepository;
import zw.miccs.shopos.fiscalisation.infrastructure.persistence.FiscalDocumentEntity;
import zw.miccs.shopos.fiscalisation.infrastructure.persistence.FiscalDocumentJpaRepository;

@Service
public class FiscalService implements FiscalDocumentService {

	private final FiscalDeviceJpaRepository devices;
	private final FiscalDocumentJpaRepository documents;
	private final FiscalDaySessionJpaRepository days;
	private final String adapter;

	public FiscalService(
		FiscalDeviceJpaRepository devices,
		FiscalDocumentJpaRepository documents,
		FiscalDaySessionJpaRepository days,
		@Value("${shopos.fiscal.adapter:MOCK}") String adapter
	) {
		this.devices = devices;
		this.documents = documents;
		this.days = days;
		this.adapter = adapter;
	}

	@Override
	@Transactional
	public FiscalDocumentView createPendingForSale(UUID tenantId, UUID saleId) {
		return documents.findByTenantIdAndSaleId(tenantId, saleId)
			.map(FiscalService::toView)
			.orElseGet(() -> {
				Instant now = Instant.now();
				FiscalDocumentEntity entity = new FiscalDocumentEntity(
					UUID.randomUUID(),
					tenantId,
					saleId,
					FiscalDocumentStatus.PENDING_SUBMISSION.name(),
					0,
					null,
					now,
					now);
				return toView(documents.saveAndFlush(entity));
			});
	}

	@Transactional(readOnly = true)
	public List<FiscalDeviceView> listDevices(UUID tenantId) {
		return devices.findByTenantIdOrderByCodeAsc(tenantId).stream()
			.map(FiscalService::toView)
			.toList();
	}

	@Transactional
	public FiscalDeviceView registerDevice(UUID tenantId, String code, String name) {
		String normalizedCode = required(code, "code");
		String normalizedName = required(name, "name");
		if (devices.findByTenantIdAndCode(tenantId, normalizedCode).isPresent()) {
			throw duplicateDevice();
		}
		FiscalDeviceEntity entity = new FiscalDeviceEntity(
			UUID.randomUUID(),
			tenantId,
			normalizedCode,
			normalizedName,
			FiscalDeviceStatus.ACTIVE.name(),
			Instant.now());
		try {
			return toView(devices.saveAndFlush(entity));
		}
		catch (DataIntegrityViolationException ex) {
			throw duplicateDevice();
		}
	}

	@Transactional(readOnly = true)
	public FiscalDocumentView getDocument(UUID tenantId, UUID documentId) {
		return documents.findByTenantIdAndId(tenantId, documentId)
			.map(FiscalService::toView)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FISCAL_DOCUMENT_NOT_FOUND",
				"Fiscal document not found", "Fiscal document does not exist"));
	}

	@Transactional
	public FiscalDocumentView retry(UUID tenantId, UUID documentId) {
		FiscalDocumentEntity entity = documents.findByTenantIdAndId(tenantId, documentId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FISCAL_DOCUMENT_NOT_FOUND",
				"Fiscal document not found", "Fiscal document does not exist"));
		entity.setStatus(FiscalDocumentStatus.PENDING_SUBMISSION.name());
		entity.setAttempts(entity.getAttempts() + 1);
		entity.setLastError(null);
		entity.setUpdatedAt(Instant.now());
		return toView(documents.save(entity));
	}

	@Transactional(readOnly = true)
	public FiscalHealthView health() {
		return new FiscalHealthView("UP", adapter);
	}

	@Transactional
	public FiscalDayView openDay(UUID tenantId) {
		if (days.findByTenantIdAndStatus(tenantId, FiscalDayStatus.OPEN.name()).isPresent()) {
			throw dayAlreadyOpen();
		}
		FiscalDaySessionEntity entity = new FiscalDaySessionEntity(
			UUID.randomUUID(),
			tenantId,
			FiscalDayStatus.OPEN.name(),
			Instant.now());
		try {
			return toView(days.saveAndFlush(entity));
		}
		catch (DataIntegrityViolationException ex) {
			throw dayAlreadyOpen();
		}
	}

	@Transactional
	public FiscalDayView closeDay(UUID tenantId) {
		FiscalDaySessionEntity entity = days.findByTenantIdAndStatus(tenantId, FiscalDayStatus.OPEN.name())
			.orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "FISCAL_DAY_NOT_OPEN", "Fiscal day not open",
				"No open fiscal day"));
		entity.setStatus(FiscalDayStatus.CLOSED.name());
		entity.setClosedAt(Instant.now());
		return toView(days.save(entity));
	}

	private static FiscalDocumentView toView(FiscalDocumentEntity entity) {
		return new FiscalDocumentView(
			entity.getId(),
			entity.getSaleId(),
			entity.getStatus(),
			entity.getAttempts(),
			entity.getLastError(),
			entity.getCreatedAt(),
			entity.getUpdatedAt());
	}

	private static FiscalDeviceView toView(FiscalDeviceEntity entity) {
		return new FiscalDeviceView(entity.getId(), entity.getCode(), entity.getName(), entity.getStatus(),
			entity.getCreatedAt());
	}

	private static FiscalDayView toView(FiscalDaySessionEntity entity) {
		return new FiscalDayView(entity.getId(), entity.getStatus(), entity.getOpenedAt(), entity.getClosedAt());
	}

	private static String required(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				field + " must not be blank");
		}
		return value.trim();
	}

	private static ApiException duplicateDevice() {
		return new ApiException(HttpStatus.CONFLICT, "DUPLICATE_FISCAL_DEVICE", "Duplicate fiscal device",
			"Fiscal device code already exists");
	}

	private static ApiException dayAlreadyOpen() {
		return new ApiException(HttpStatus.CONFLICT, "FISCAL_DAY_ALREADY_OPEN", "Fiscal day already open",
			"A fiscal day is already open");
	}

}
