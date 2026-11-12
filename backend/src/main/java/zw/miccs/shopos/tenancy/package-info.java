/**
 * Tenants, memberships, tenant context and RLS setup.
 *
 * <p>Other modules call this one only through its public service interface in
 * {@code application}; they never read its tables or repositories directly.
 */
package zw.miccs.shopos.tenancy;
