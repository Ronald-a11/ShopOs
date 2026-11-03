package zw.miccs.shopos.common.api;

public record ProblemDetailBody(
	String type,
	String title,
	int status,
	String code,
	String detail,
	String traceId
) {
}
