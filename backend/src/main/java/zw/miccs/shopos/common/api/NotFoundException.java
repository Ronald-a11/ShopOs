package zw.miccs.shopos.common.api;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {

	public NotFoundException(String code, String detail) {
		super(HttpStatus.NOT_FOUND, code, detail);
	}
}
