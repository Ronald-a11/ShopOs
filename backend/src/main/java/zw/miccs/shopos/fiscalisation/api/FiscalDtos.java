package zw.miccs.shopos.fiscalisation.api;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class FiscalDtos {

	private FiscalDtos() {
	}

	public record RegisterFiscalDeviceRequest(
		@NotBlank @Size(max = 64) String code,
		@NotBlank @Size(max = 200) String name
	) {
	}

	public record FiscalDeviceResponse(
		UUID id,
		String code,
		String name,
		String status,
		Instant createdAt
	) {
	}

	public record FiscalDocumentResponse(
		UUID id,
		UUID saleId,
		String status,
		int attempts,
		String lastError,
		Instant createdAt,
		Instant updatedAt
	) {
	}

	public record FiscalHealthResponse(
		String status,
		String adapter
	) {
	}

	public record FiscalDayResponse(
		UUID id,
		String status,
		Instant openedAt,
		Instant closedAt
	) {
	}

}
