/**
 * Bootstrap, batch replay, idempotency and acknowledgements.
 *
 * <p>Other modules call this one only through its public service interface in
 * {@code application}; they never read its tables or repositories directly.
 */
package zw.miccs.shopos.synchronisation;
