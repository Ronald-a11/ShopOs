package zw.miccs.shopos.catalogue.domain;

import java.util.List;

public record ProductImportResult(
	int created,
	int updated,
	List<RowError> errors
) {
	public record RowError(int row, String message) {
	}
}
