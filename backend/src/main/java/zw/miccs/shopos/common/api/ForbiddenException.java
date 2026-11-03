package zw.miccs.shopos.common.api;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException {

	public ForbiddenException(String code, String detail) {
		super(HttpStatus.FORBIDDEN, code, detail);
	}
}
