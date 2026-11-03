package zw.miccs.shopos.common.api;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {

	public ConflictException(String code, String detail) {
		super(HttpStatus.CONFLICT, code, detail);
	}
}
