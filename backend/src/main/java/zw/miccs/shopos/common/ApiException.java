package zw.miccs.shopos.common;

import org.springframework.http.HttpStatus;

/**
 * Domain/application failure mapped to RFC 7807 problem+json by {@link ProblemDetailExceptionHandler}.
 */
public class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final String code;
	private final String title;

	public ApiException(HttpStatus status, String code, String title, String detail) {
		super(detail);
		this.status = status;
		this.code = code;
		this.title = title;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

}
