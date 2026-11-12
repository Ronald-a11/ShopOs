package zw.miccs.shopos.catalogue.api;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public final class CatalogueDtos {

	private CatalogueDtos() {
	}

	public record CreateProductRequest(
		@NotBlank @Size(max = 64) String sku,
		@NotBlank @Size(max = 200) String name,
		@Size(max = 64) String barcode,
		@NotNull @PositiveOrZero BigDecimal unitPrice,
		@Size(max = 32) String taxRateCode
	) {
	}

	public record ProductResponse(
		UUID id,
		String sku,
		String name,
		String barcode,
		BigDecimal unitPrice,
		String taxRateCode,
		String status
	) {
	}

}
