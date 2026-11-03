package zw.miccs.shopos.common.api;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ApiException {

	public UnprocessableEntityException(String code, String detail) {
		super(HttpStatus.UNPROCESSABLE_ENTITY, code, detail);
	}
}
