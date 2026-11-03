package zw.miccs.shopos.branches.domain;

import java.util.UUID;

public record Branch(
	UUID id,
	UUID tenantId,
	String code,
	String name,
	String address,
	boolean active
) {
}
