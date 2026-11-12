package zw.miccs.shopos.sales.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Read model for reporting. Reporting must depend on this interface, not on sales persistence.
 */
public interface SalesQueryService {

	SalesReportView salesReport(UUID tenantId, UUID branchId, Instant from, Instant to);

}
