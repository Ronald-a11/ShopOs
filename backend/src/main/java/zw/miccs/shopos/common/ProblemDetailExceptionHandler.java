package zw.miccs.shopos.common;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProblemDetailExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ProblemDetailExceptionHandler.class);
	private static final String ERROR_DOCS = "https://docs.shopos.co.zw/errors/";

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ProblemDetail> handleApi(ApiException ex) {
		return ResponseEntity.status(ex.getStatus())
			.contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.body(problem(ex.getStatus(), ex.getCode(), ex.getTitle(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		FieldError field = ex.getBindingResult().getFieldError();
		String detail = field == null
			? "Request validation failed"
			: field.getField() + " " + field.getDefaultMessage();
		return ResponseEntity.badRequest()
			.contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.body(problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed", detail));
	}

	@ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
	ResponseEntity<ProblemDetail> handleAuth(AuthenticationException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.body(problem(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "Unauthenticated",
				"Authentication is required"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.body(problem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Access denied",
				"You do not have permission to perform this action"));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest()
			.contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.body(problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed", "Request body is invalid"));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ProblemDetail> handleConflict(DataIntegrityViolationException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.body(problem(HttpStatus.CONFLICT, "CONFLICT", "Conflict",
				"The request conflicts with existing data"));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
		log.error("Unhandled exception", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.contentType(MediaType.APPLICATION_PROBLEM_JSON)
			.body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Internal error",
				"An unexpected error occurred"));
	}

	private static ProblemDetail problem(HttpStatus status, String code, String title, String detail) {
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setTitle(title);
		pd.setType(URI.create(ERROR_DOCS + code.toLowerCase().replace('_', '-')));
		pd.setProperty("code", code);
		pd.setProperty("traceId", UUID.randomUUID().toString().substring(0, 8));
		return pd;
	}

}
