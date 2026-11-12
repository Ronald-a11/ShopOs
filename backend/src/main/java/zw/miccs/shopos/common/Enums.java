package zw.miccs.shopos.common;

import java.util.Locale;

import org.springframework.http.HttpStatus;

public final class Enums {

	private Enums() {
	}

	public static <E extends Enum<E>> E parse(Class<E> type, String raw, String field) {
		if (raw == null || raw.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				field + " is required");
		}
		try {
			return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
				field + " is invalid");
		}
	}

}
