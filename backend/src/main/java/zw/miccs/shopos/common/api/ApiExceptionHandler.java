package zw.miccs.shopos.common.api;

import java.net.URI;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final String TYPE_BASE = "https://docs.shopos.co.zw/errors/";

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ProblemDetailBody> handleApi(ApiException ex) {
		return problem(ex.getStatus(), ex.getCode(), ex.getStatus().getReasonPhrase(), ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetailBody> handleValidation(MethodArgumentNotValidException ex) {
		FieldError field = ex.getBindingResult().getFieldError();
		String detail = field == null ? "Validation failed" : field.getField() + " " + field.getDefaultMessage();
		return problem(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "Validation failed", detail);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetailBody> handleGeneric(Exception ex) {
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal Server Error",
			"An unexpected error occurred");
	}

	private ResponseEntity<ProblemDetailBody> problem(HttpStatus status, String code, String title, String detail) {
		String traceId = MDC.get("traceId");
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
		}
		ProblemDetailBody body = new ProblemDetailBody(
			URI.create(TYPE_BASE + code.toLowerCase().replace('_', '-')).toString(),
			title,
			status.value(),
			code,
			detail,
			traceId);
		return ResponseEntity.status(status)
			.contentType(MediaType.parseMediaType("application/problem+json"))
			.body(body);
	}
}
