package zw.miccs.shopos.fiscalisation.application;

import java.util.UUID;

/**
 * Public fiscal document port. Sales creates a pending document here and never touches fiscal tables.
 */
public interface FiscalDocumentService {

	FiscalDocumentView createPendingForSale(UUID tenantId, UUID saleId);

}
