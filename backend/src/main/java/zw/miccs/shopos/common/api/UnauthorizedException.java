package zw.miccs.shopos.common.api;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

	public UnauthorizedException(String code, String detail) {
		super(HttpStatus.UNAUTHORIZED, code, detail);
	}
}
